package com.azki.reservation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseException {
    private static final Logger log = LoggerFactory.getLogger(NotFoundException.class);
    public NotFoundException() {
        super("not found!", HttpStatus.NOT_FOUND);
        log.error(
                String.valueOf(this)
        );
    }
    public NotFoundException(String msg) {
        super(String.format("%s",msg), HttpStatus.NOT_FOUND);
        log.error(
                String.valueOf(this)
        );
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
