package com.lyh.live.config;

import com.lyh.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveWebSocketHandler liveWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 配置路由规则：前端连接的 URL 为 /ws/live/{roomId}
        // 这里的 * 会被 Handler 内部的 extractRoomId 解析
        registry.addHandler(liveWebSocketHandler, "/ws/live/*")
                .setAllowedOrigins("*"); // 允许跨域
    }
}
