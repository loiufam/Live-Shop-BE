package com.lyh.user.exception;

import com.lyh.common.result.CodeMsg;

public class UserCodeMsg extends CodeMsg {
    
    private UserCodeMsg(Integer code, String msg) {
        super(code, msg);
    }
    
    // 用户相关错误码 1001-1999
    public static final UserCodeMsg USERNAME_EXIST = new UserCodeMsg(1001, "用户名已注册");
    public static final UserCodeMsg USER_NOT_FOUND = new UserCodeMsg(1002, "用户不存在");
    public static final UserCodeMsg PASSWORD_ERROR = new UserCodeMsg(1003, "密码错误");
    public static final UserCodeMsg USERNAME_OR_PASSWORD_EMPTY = new UserCodeMsg(1004, "用户名或密码不能为空");
}
