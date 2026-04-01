package com.lyh.live.service;

import com.lyh.live.entity.SrsClientsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
public class SrsService {

    @Value("${srs.host}")
    private String srsHost;

    @Value("${srs.http-port}")
    private int httpPort;

    @Value("${srs.rtc-port}")
    private int rtcPort;

    @Value("${srs.rtmp-port}")
    private int rtmpPort;

    @Value("${srs.app:live}") // 默认 app 为 live
    private String app;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 生成唯一的推流 key
     */
    public String generateStreamKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 为 Controller 提供：获取主播推流地址 (RTMP 格式，适配 OBS)
     * 你的前端目前用的是 flv.js 拉流，通常主播端会用 OBS 等工具推流 (RTMP)
     */
    public String generatePushUrl(Long roomId, String streamKey) {
        // 格式: rtmp://{host}:{port}/{app}/{stream}?token={streamKey}
        // stream 名称建议使用 roomId，方便管理
        return String.format("rtmp://%s:%d/%s/%d?token=%s", srsHost, rtmpPort, app, roomId, streamKey);
    }

    /**
     * 为 Controller 提供：获取观众拉流地址 (HTTP-FLV 格式)
     * 对应前端的 mpegts.js / flv.js
     */
    public String generatePullUrl(Long roomId) {
        // 格式: http://{host}:8080/{app}/{stream}.flv
        return String.format("http://%s:8080/%s/%d.flv", srsHost, app, roomId);
    }

    /**
     * 强制踢掉某个直播流 (用于主播结束直播或违规封禁)
     * 通过调用 SRS 的 HTTP API: DELETE /api/v1/clients/{client_id}
     */
    public void kickStream(Long roomId) {
        try {
            // 1. 先查询该流的 clientId
            String streamName = String.valueOf(roomId);
            String apiUrl = String.format("http://%s:%d/api/v1/clients?pageSize=100", srsHost, httpPort);
            SrsClientsResponse response = restTemplate.getForObject(apiUrl, SrsClientsResponse.class);

            if (response == null || response.getClients() == null) return;

            // 2. 找到对应的 publish 客户端并发送 DELETE 请求
            response.getClients().stream()
                    .filter(c -> streamName.equals(c.getStream()) && "publish".equals(c.getType()))
                    .findFirst()
                    .ifPresent(client -> {
                        String deleteUrl = String.format("http://%s:%d/api/v1/clients/%s", srsHost, httpPort, client.getId());
                        restTemplate.delete(deleteUrl);
                        log.info("成功踢出直播流, roomId: {}, clientId: {}", roomId, client.getId());
                    });
        } catch (Exception e) {
            log.error("踢出直播流失败, roomId: {}", roomId, e);
        }
    }
}
