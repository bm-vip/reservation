package com.azki.reservation;

import com.azki.reservation.common.util.JWTUtil;
import com.azki.reservation.common.util.RequestHolder;
import com.azki.reservation.entity.UserEntity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class JWTUtilTest {
    private Environment environment = Mockito.mock(Environment.class);
    @Test
    public void getTokenAndExtractUsername() {
        //mock data
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("b.mohamadi");
        userEntity.setEmail("b.mohamadi@gmail.com");
        userEntity.setId(UUID.randomUUID());

        when(environment.getProperty("token.expiration_time")).thenReturn("2592000000");
        when(environment.getProperty("token.secret")).thenReturn("DDEF552222CB580670D7C428E7BAC5584AE930D05B5A20F3582892CE6720A3EB");

        //test generateToken
        String token = new JWTUtil(environment).generateToken(userEntity);
        assertTrue(token != null && !token.isEmpty());

        var userName = RequestHolder.getUserName(token);
        assertEquals(userEntity.getUsername(), userName);
    }
}