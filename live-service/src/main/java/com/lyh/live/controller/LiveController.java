package com.lyh.live.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.lyh.common.annotation.RequireLogin;
import com.lyh.common.context.UserContext;
import com.lyh.common.result.Result;
import com.lyh.live.dto.CreateRoomReqDTO;
import com.lyh.live.dto.LiveRoomRespDTO;
import com.lyh.live.dto.SrsCallbackDTO;
import com.lyh.live.dto.StreamInfoDTO;
import com.lyh.live.entity.LiveRoom;
import com.lyh.live.service.LiveRoomService;
import com.lyh.live.service.SrsService;
import com.lyh.live.vo.PageResult;
import com.lyh.live.websocket.LiveWebSocketHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveRoomService liveRoomService;
    private final SrsService srsService;
    private final LiveWebSocketHandler wsHandler;
    private final StringRedisTemplate redisTemplate;
    
    private static final String LIVE_ROOM_TOKEN_PREFIX = "live:room:";
    private static final long LIVE_ROOM_TOKEN_EXPIRE = 24; // 小时

    /**
     * 获取正在直播的列表 (支持分页)
     */
    @RequireLogin
    @GetMapping("/rooms")
    public Result<PageResult<LiveRoom>> getRooms(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 1. 调用 Service 层进行分页查询，只查 status = 1 (开播中) 的房间
        PageResult<LiveRoom> result = liveRoomService.getLiveRoomsPage(page, pageSize);

        return Result.success(result);
    }
    
    /**
     * 获取直播间详情
     */
    @RequireLogin
    @GetMapping("/rooms/{roomId}")
    public Result<LiveRoomRespDTO> getRoomDetail(@PathVariable Long roomId) {
        LiveRoom room = liveRoomService.findRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("直播间不存在"));
        
        LiveRoomRespDTO respDTO = new LiveRoomRespDTO();
        respDTO.setId(room.getId());
        respDTO.setTitle(room.getTitle());
        respDTO.setAnchorId(room.getAnchorId());
        respDTO.setCoverImg(room.getCoverImg());
        respDTO.setStatus(room.getStatus());
        respDTO.setCreateTime(room.getCreateTime());
        respDTO.setViewerCount(wsHandler.getRoomViewerCount(roomId));
        
        return Result.success(respDTO);
    }
    
    /**
     * 获取直播间当前观看人数
     */
    @GetMapping("/rooms/{roomId}/viewers")
    public Result<Integer> getRoomViewers(@PathVariable Long roomId) {
        int viewerCount = wsHandler.getRoomViewerCount(roomId);
        return Result.success(viewerCount);
    }

    /**
     * 获取推流/拉流地址
     */
    @RequireLogin
    @GetMapping("/rooms/{roomId}/stream")
    public Result<StreamInfoDTO> getStreamInfo(@PathVariable Long roomId, 
                                                @RequestParam(defaultValue = "viewer") String role) {
        // 1. 检查直播间是否存在
        LiveRoom room = liveRoomService.findActiveRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("直播间不存在或已结束"));

        // 2. 组装流地址信息
        StreamInfoDTO streamInfo = new StreamInfoDTO();
        // 拉流地址所有人都可以看 (这里调用 SrsService 生成前端所需的 FLV/HLS 播放地址)
        streamInfo.setPullUrl(srsService.generatePullUrl(room.getId()));

        // 只有主播才能获取推流地址
        if ("anchor".equals(role)) {
            Long currentUserId = UserContext.getUserId();
            if (currentUserId == null || !currentUserId.equals(room.getAnchorId())) {
                return Result.error("无权获取推流地址");
            }
            streamInfo.setPushUrl(srsService.generatePushUrl(room.getId(), room.getStreamKey()));
        }

        return Result.success(streamInfo);
    }

    /**
     * 创建直播间（开播）
     */
    @RequireLogin
    @PostMapping("/rooms")
    public Result<LiveRoomRespDTO> createRoom(@Valid @RequestBody CreateRoomReqDTO req) {
        // 从上下文获取当前登录用户ID
        Long anchorId = UserContext.getUserId();
        if (anchorId == null) {
            return Result.error("请先登录");
        }
        
        // 检查用户是否已经有正在直播的房间
        if (liveRoomService.hasActiveRoom(anchorId)) {
            return Result.error("您已经有一个正在直播的房间");
        }

        // 生成推流密钥 (streamKey)。这个密钥会在 SRS 回调时用于鉴权
        String streamKey = srsService.generateStreamKey();

        // 保存房间信息到数据库
        LiveRoom room = new LiveRoom();
        room.setAnchorId(anchorId);
        room.setTitle(req.getTitle());
        room.setCoverImg(req.getCoverImg());
        room.setStreamKey(streamKey);
        room.setStatus(0); // 0 表示准备中，等 SRS 回调 on_publish 再改为 1

        liveRoomService.save(room);
        
        // 将 streamKey 存入 Redis，用于 SRS 回调鉴权
        String redisKey = LIVE_ROOM_TOKEN_PREFIX + room.getId();
        redisTemplate.opsForValue().set(redisKey, streamKey, LIVE_ROOM_TOKEN_EXPIRE, TimeUnit.HOURS);
        
        // 构建返回对象
        LiveRoomRespDTO respDTO = new LiveRoomRespDTO();
        respDTO.setId(room.getId());
        respDTO.setTitle(room.getTitle());
        respDTO.setAnchorId(room.getAnchorId());
        respDTO.setCoverImg(room.getCoverImg());
        respDTO.setStatus(room.getStatus());
        respDTO.setPushUrl(srsService.generatePushUrl(room.getId(), streamKey));
        respDTO.setPullUrl(srsService.generatePullUrl(room.getId()));

        return Result.success(respDTO);
    }

    /**
     * 结束直播
     */
    @RequireLogin
    @PutMapping("/rooms/{roomId}/close")
    public Result<Void> closeRoom(@PathVariable Long roomId) {
        Long currentUserId = UserContext.getUserId();

        // 1. 查询房间并校验权限
        LiveRoom room = liveRoomService.findActiveRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("直播间不存在"));
        
        // 校验是否是房间主播
        if (!room.getAnchorId().equals(currentUserId)) {
            return Result.error("无权关闭此直播间");
        }

        // 2. 更新数据库状态为结束 (status = 2)
        room.setStatus(2);
        liveRoomService.updateRoom(room);
        
        // 删除 Redis 中的 token
        redisTemplate.delete(LIVE_ROOM_TOKEN_PREFIX + roomId);

        // 3. 通过 SRS API 强制踢掉该流，防止主播使用软件继续推流
        srsService.kickStream(roomId);

        // 4. 通过 WebSocket 通知房间内的观众直播已结束
        wsHandler.broadcastLiveEnd(roomId);

        return Result.success(null);
    }

    /**
     * SRS 回调鉴权接口 (供 SRS 服务器内部调用)
     * 注意：SRS 回调要求返回 HTTP 200，内容为 "0" 代表允许，非 "0" 代表拒绝
     */
    @PostMapping("/srs/callback")
    public String srsCallback(@RequestBody SrsCallbackDTO dto) {
        log.info("SRS 回调: action={}, stream={}, param={}", dto.getAction(), dto.getStream(), dto.getParam());
        
        if ("on_publish".equals(dto.getAction())) {
            String roomIdStr = dto.getStream(); // stream 对应 roomId
            String param = dto.getParam();   // 例如 "?token=xxx"

            if (StringUtils.isBlank(param) || !param.contains("token=")) {
                log.warn("推流被拒绝: 缺少token参数, roomId={}", roomIdStr);
                return "1"; // 拒绝推流
            }

            // 解析出 token
            String pushToken = param.split("token=")[1].split("&")[0];

            // 从 Redis 校验该房间号对应的 token 是否正确
            String redisKey = LIVE_ROOM_TOKEN_PREFIX + roomIdStr;
            String validToken = redisTemplate.opsForValue().get(redisKey);

            if (validToken != null && validToken.equals(pushToken)) {
                // 鉴权通过，更新直播间状态为"直播中"
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    liveRoomService.updateRoomStatus(roomId, 1);
                    log.info("推流成功，直播间已开始: roomId={}", roomId);
                } catch (Exception e) {
                    log.error("更新直播间状态失败", e);
                }
                return "0";
            } else {
                log.warn("推流被拒绝: token不匹配, roomId={}", roomIdStr);
                return "1"; // Token 不匹配，拒绝推流
            }
        }

        if ("on_unpublish".equals(dto.getAction())) {
            // 主播断开连接，更新数据库，将直播间状态改为 "休息中"
            String roomIdStr = dto.getStream();
            try {
                Long roomId = Long.parseLong(roomIdStr);
                liveRoomService.updateRoomStatus(roomId, 0);
                log.info("主播断开连接，直播间已暂停: roomId={}", roomId);
            } catch (Exception e) {
                log.error("更新直播间状态失败", e);
            }
            return "0";
        }

        return "0"; // 其他事件默认放行
    }
}
