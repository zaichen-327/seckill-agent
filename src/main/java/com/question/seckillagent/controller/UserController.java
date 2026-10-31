package com.question.seckillagent.controller;

import com.question.seckillagent.common.Result;
import com.question.seckillagent.dto.UserLoginRequest;
import com.question.seckillagent.dto.UserRegisterRequest;
import com.question.seckillagent.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterRequest request) {
        log.info("用户注册请求，username={}", request.getUsername());
        userService.register(request.getUsername(), request.getPassword());
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginRequest request){
        log.info("用户登录请求，username={}", request.getUsername());
        String token = userService.login(request.getUsername(), request.getPassword());
        return Result.success(token);
    }
}
