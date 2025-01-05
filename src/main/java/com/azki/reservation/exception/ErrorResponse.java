package com.azki.reservation.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String path;
    private final Object message;
    private String code;

    public ErrorResponse(HttpStatus status, String path, Object message, String code) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.path = path;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
    public ErrorResponse(HttpStatus status, String path, Object message) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.path = path;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
