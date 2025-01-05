package com.azki.reservation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends BaseException { //to check duplicate entry
    private static final Logger log = LoggerFactory.getLogger(ConflictException.class);
    public ConflictException(String field, String tableName) {
        super(String.format("%s is already in use in %s.", field, tableName), HttpStatus.CONFLICT);
        log.error(
                String.valueOf(this)
        );
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
