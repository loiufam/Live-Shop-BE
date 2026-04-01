package com.lyh.common.context;

import com.lyh.common.domin.UserInfo;

/**
 * 用户上下文持有者，使用 ThreadLocal 存储当前线程的用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前用户信息
     */
    public static void setUser(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }
    
    /**
     * 获取当前用户信息
     */
    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo != null ? userInfo.getId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }
    
    /**
     * 清除当前用户信息（请求结束后调用）
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
