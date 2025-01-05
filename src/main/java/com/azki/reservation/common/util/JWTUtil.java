package com.azki.reservation.common.util;

import com.azki.reservation.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;

import java.util.Date;

public class JWTUtil {
    private final Environment environment;

    public JWTUtil(Environment environment) {
        this.environment = environment;
    }

    public String generateToken(UserEntity userEntity) {
        var expirationTime = environment.getProperty("token.expiration_time");
        expirationTime = expirationTime == null ? "3600000": expirationTime;
        var secret = environment.getProperty("token.secret");
        var builder = Jwts.builder()
                .setId(userEntity.getId().toString())
                .setSubject(userEntity.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expirationTime)))
                .setIssuer("reservation")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secret)
                ;

        return builder.compact();
    }
}
