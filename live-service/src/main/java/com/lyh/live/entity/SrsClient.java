package com.lyh.live.entity;

import lombok.Data;

@Data
public class SrsClient {
    private String id;     // 必须有 id 才能踢人
    private String stream;
    private String type;
}
