package com.recitapp.recitapp_api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecitappException extends RuntimeException {
    public RecitappException(String message) {
        super(message);
    }
}