package com.lyh.user.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private BigDecimal balance;
}
