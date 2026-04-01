package com.lyh.common.domin;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {
    private Long id;        // 用户ID
    private String username; // 用户名
    private Long phone;
    private String nickName;
    private String head;
    private String birthDay;
    private String info;
    private String loginIp;
}
