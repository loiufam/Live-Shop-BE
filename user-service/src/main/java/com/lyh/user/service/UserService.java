package com.lyh.user.service;

public interface UserService {

    void register(String username, String password);

    String login(String username, String password);
}
