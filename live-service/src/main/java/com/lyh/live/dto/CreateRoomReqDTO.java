package com.lyh.live.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoomReqDTO {
    
    @NotBlank(message = "直播间标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度应在1-50个字符之间")
    private String title;
    
    private String coverImg; // 封面图，可选
    
    // anchorId 应该从登录用户上下文获取，而不是前端传入
    // private Long anchorId;
}
