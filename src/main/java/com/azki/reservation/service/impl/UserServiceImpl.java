package com.azki.reservation.service.impl;

import com.azki.reservation.common.util.JWTUtil;
import com.azki.reservation.entity.UserEntity;
import com.azki.reservation.exception.NotFoundException;
import com.azki.reservation.repository.UserRepository;
import com.azki.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Environment environment;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password) {
        var user = userRepository.findByUsername(username).orElseThrow(()->new NotFoundException("user not found"));
        boolean passwordMatches = bCryptPasswordEncoder.matches(password, user.getPassword());
        if (!passwordMatches)
            throw new NotFoundException("user not found");

        Map<String, Object> map = new HashMap<>();
        map.put("token", getToken(user));
        map.put("user", user);
        return map;
    }
    private String getToken(UserEntity user) {
        JWTUtil jwtUtil = new JWTUtil(environment);
        return jwtUtil.generateToken(user);
    }
}
