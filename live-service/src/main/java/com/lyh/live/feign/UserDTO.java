package com.lyh.live.feign;

import lombok.Data;

/**
 * 用户信息 DTO (用于 Feign 调用)
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}
