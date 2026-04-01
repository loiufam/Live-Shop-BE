package com.lyh.live.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SrsCallbackDTO {
    
    private String action;   // 事件类型，如 "on_publish"
    
    @JsonProperty("client_id")
    @JsonAlias("clientId")
    private String clientId;
    
    private String ip;
    private String vhost;
    private String app;      // 应用名，通常是 "live"
    private String stream;   // 流名称，我们通常用 roomId
    private String param;    // URL 参数，比如 "?token=xxx"
    
    @JsonProperty("tcUrl")
    @JsonAlias("tc_url")
    private String tcUrl;    // 推流地址
    
    @JsonProperty("pageUrl")
    @JsonAlias("page_url")
    private String pageUrl;  // 页面地址
}
