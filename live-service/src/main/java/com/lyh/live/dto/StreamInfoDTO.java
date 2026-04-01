package com.lyh.live.dto;

import lombok.Data;

@Data
public class StreamInfoDTO {
    private String pullUrl;  // 观众拉流地址 (例如 http://127.0.0.1:8080/live/{roomId}.flv)
    private String pushUrl;  // 主播推流地址 (例如 rtmp://127.0.0.1/live/{roomId}?token={streamKey})
}
