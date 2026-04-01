package com.lyh.user.controller;

import com.lyh.common.annotation.RequireLogin;
import com.lyh.common.result.Result;
import com.lyh.user.dto.LoginDTO;
import com.lyh.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.lyh.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody LoginDTO dto) {
        userService.register(dto.getUsername(), dto.getPassword());
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", dto.getUsername());

        return Result.success("登录成功", data);
    }
    
    /**
     * 根据用户ID获取用户信息 (供 Feign 调用)
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }
    
    /**
     * 获取当前登录用户信息
     */
    @RequireLogin
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }
}
