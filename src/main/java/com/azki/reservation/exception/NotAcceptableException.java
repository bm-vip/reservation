package com.azki.reservation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NotAcceptableException extends BaseException { // to check validations
    private static final Logger log = LoggerFactory.getLogger(NotAcceptableException.class);
    public NotAcceptableException() {
        super("not accepted!",HttpStatus.NOT_ACCEPTABLE);
        log.error(
                String.valueOf(this)
        );
    }
    public NotAcceptableException(String msg) {
        super(String.format("%s",msg), HttpStatus.NOT_ACCEPTABLE);
        log.error(
                String.valueOf(this)
        );
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
