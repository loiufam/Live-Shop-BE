package com.lyh.user.service;

import com.lyh.user.vo.UserVO;

public interface UserService {

    void register(String username, String password);

    String login(String username, String password);
    
    /**
     * 根据用户ID获取用户信息
     */
    UserVO getUserById(Long userId);
    
    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();
}
