package com.question.seckillagent.service;

import java.util.Map;

public interface UserService {
    void register(String userName, String password);

    Map<String, Object> login(String username, String password);
}
