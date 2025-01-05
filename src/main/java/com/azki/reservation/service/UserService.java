package com.azki.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface UserService {
    Map<String,Object> login(String username, String password) throws JsonProcessingException;
}
