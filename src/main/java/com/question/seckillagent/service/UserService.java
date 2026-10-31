package com.question.seckillagent.service;

public interface UserService {
    void register(String userName, String password);

    String login(String username, String password);
}
