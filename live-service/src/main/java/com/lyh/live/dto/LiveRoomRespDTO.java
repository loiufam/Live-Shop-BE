package com.lyh.live.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LiveRoomRespDTO {
    private Long id;
    private String title;
    private Long anchorId;
    private String coverImg;
    private Integer status;        // 0-准备中, 1-直播中, 2-已结束
    private LocalDateTime createTime;
    private Integer viewerCount;   // 当前观看人数
    private String pushUrl;        // 推流地址 (仅主播可见)
    private String pullUrl;        // 拉流地址
}
