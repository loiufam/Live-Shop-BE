package com.lyh.user.controller;

import com.lyh.user.dto.LoginDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lyh.user.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody LoginDTO dto) {
        userService.register(dto.getUsername(), dto.getPassword());
        return "注册成功";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDTO dto) {
        return userService.login(dto.getUsername(), dto.getPassword());
    }
}
