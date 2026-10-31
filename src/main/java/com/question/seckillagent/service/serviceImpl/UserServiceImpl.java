package com.question.seckillagent.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.entity.SeckillUser;
import com.question.seckillagent.mapper.SeckillUserMapper;
import com.question.seckillagent.service.UserService;
import com.question.seckillagent.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final SeckillUserMapper seckillUserMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public UserServiceImpl(SeckillUserMapper seckillUserMapper, JwtUtil jwtUtil) {
        this.seckillUserMapper = seckillUserMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        QueryWrapper<SeckillUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (seckillUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        SeckillUser user = new SeckillUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        seckillUserMapper.insert(user);
        log.info("用户注册成功，username={}", username);
    }

    @Override
    public String login(String username, String password) {
        QueryWrapper<SeckillUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        SeckillUser user = seckillUserMapper.selectOne(wrapper);
        if(user == null || !passwordEncoder.matches(password, user.getPasswordHash())){
            throw new BusinessException("用户名或者密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功，username={}", username);
        return token;
    }
}