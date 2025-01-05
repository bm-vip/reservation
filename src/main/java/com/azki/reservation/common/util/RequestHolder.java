package com.azki.reservation.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Map;


@Component
@RequestScope
@Slf4j
public class RequestHolder {
    private final HttpServletRequest request;
    public RequestHolder() {
        this.request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public String getToken() {
        String authorization = request.getHeader("Authorization");
        if(StringUtils.hasLength(authorization))
            return authorization.substring(7);
        return "";
    }

    public static String getUserName(String token) {
        try {
            // Split the token into header, payload, and signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }

            // Decode the payload (second part of the token)
            String payload = new String(Base64.getDecoder().decode(parts[1]));

            // Use Jackson to parse the JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            var claims = objectMapper.readValue(payload, Map.class);

            // Extract the subject
            return (String) claims.get("sub");  // "sub" is the subject claim
        } catch (Exception e) {
            log.error("Failed to decode token.", e);
            return null;
        }
    }
}
