package com.lyh.live.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {

    // 房间映射：Map<RoomId, Set<WebSocketSession>>
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 统计各房间人数：Map<RoomId, AtomicInteger>
    private final Map<Long, AtomicInteger> roomViewers = new ConcurrentHashMap<>();

    // 记录 Session 与 RoomId 的关系，方便断开连接时快速清理
    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 核心亮点：使用独立线程池异步广播消息，避免阻塞 Netty/Tomcat 的 IO 线程
    // 线程池参数可根据实际机器配置调整
    private final ExecutorService broadcastExecutor = new ThreadPoolExecutor(
            10, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满了调用者自己执行，保证消息不丢
    );

    /**
     * 建立连接后：加入房间，更新人数，广播通知
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = extractRoomId(session);
        if (roomId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 1. 将 Session 加入房间集合
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoomMap.put(session.getId(), roomId);

        // 2. 增加房间人数
        AtomicInteger viewerCount = roomViewers.computeIfAbsent(roomId, k -> new AtomicInteger(0));
        int currentCount = viewerCount.incrementAndGet();

        log.info("用户加入房间 {}, session: {}, 当前人数: {}", roomId, session.getId(), currentCount);

        // 3. (可选) 异步广播有人加入的消息及最新人数给前端更新 UI
        // broadcastToRoom(roomId, "{\"type\":\"viewer_join\", \"totalViewers\":" + currentCount + "}");
    }

    /**
     * 收到前端消息：解析并广播到整个房间
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long roomId = sessionRoomMap.get(session.getId());
        if (roomId == null) return;

        String payload = message.getPayload();
        // 此处可以加入敏感词过滤等逻辑...

        // 将消息（弹幕/礼物）广播给同房间的所有人
        broadcastToRoom(roomId, payload);
    }

    /**
     * 断开连接：清理资源，更新人数
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                // 减少人数
                AtomicInteger viewerCount = roomViewers.get(roomId);
                if (viewerCount != null) {
                    viewerCount.decrementAndGet();
                }
                log.info("用户离开房间 {}, session: {}", roomId, session.getId());
            }
        }
    }

    /**
     * 异步广播消息到指定房间
     */
    private void broadcastToRoom(Long roomId, String messagePayload) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return;

        TextMessage textMessage = new TextMessage(messagePayload);

        // 遍历发送。因为 Session 的 sendMessage 是同步阻塞的，所以放入线程池异步发送
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                broadcastExecutor.submit(() -> {
                    try {
                        // synchronized 保证同一个 session 不会并发写入 (Spring WebSocket 的强制要求)
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    } catch (IOException e) {
                        log.warn("向 session {} 发送消息失败", session.getId(), e);
                    }
                });
            }
        }
    }

    /**
     * 供外部 Controller 调用：广播直播结束通知
     */
    public void broadcastLiveEnd(Long roomId) {
        String msg = "{\"type\":\"live_end\", \"message\":\"主播已结束直播\"}";
        broadcastToRoom(roomId, msg);

        // 延迟清理房间结构（等消息发完）
        broadcastExecutor.submit(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            roomSessions.remove(roomId);
            roomViewers.remove(roomId);
        });
    }

    /**
     * 获取指定房间当前在线人数 (供 HTTP 接口调用)
     */
    public int getRoomViewerCount(Long roomId) {
        AtomicInteger count = roomViewers.get(roomId);
        return count == null ? 0 : count.get();
    }

    /**
     * 解析 URI 获取 roomId
     * 假设前端连接地址为: ws://localhost:8080/ws/live/1001?token=xxx
     */
    private Long extractRoomId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            String path = uri.getPath();
            // 简单截取最后一个 '/' 后的内容作为 roomId
            String[] segments = path.split("/");
            return Long.parseLong(segments[segments.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
