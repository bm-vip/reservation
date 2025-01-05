package com.azki.reservation.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException {
    private static final Logger log = LoggerFactory.getLogger(BadRequestException.class);
    public BadRequestException() {
        super("bad request!", HttpStatus.BAD_REQUEST);
        log.error(
                String.valueOf(this)
        );
    }
    public BadRequestException(String msg) {
        super(String.format("%s",msg), HttpStatus.BAD_REQUEST);
        log.error(
                String.valueOf(this)
        );
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
