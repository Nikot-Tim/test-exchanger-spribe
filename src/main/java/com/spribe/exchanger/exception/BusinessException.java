package com.spribe.exchanger.exception;

import com.spribe.exchanger.error.ErrorHolder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final transient ErrorHolder errorHolder;

    public BusinessException(HttpStatus httpStatus, ErrorHolder errorHolder) {
        super(errorHolder.getCode().name());
        this.httpStatus = httpStatus;
        this.errorHolder = errorHolder;
    }
}
