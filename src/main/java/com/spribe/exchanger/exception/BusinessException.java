package com.spribe.exchanger.exception;

import com.spribe.exchanger.error.ErrorCode;
import com.spribe.exchanger.error.ErrorHolder;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final transient ErrorHolder errorHolder;

    public BusinessException(HttpStatus httpStatus, ErrorHolder errorHolder) {
        super(errorHolder.getCode().name());
        this.httpStatus = httpStatus;
        this.errorHolder = errorHolder;
    }

    public BusinessException(HttpStatus httpStatus, ErrorHolder errorHolder, Throwable cause) {
        super(errorHolder.getCode().name(), cause);
        this.httpStatus = httpStatus;
        this.errorHolder = errorHolder;
    }

    public ErrorCode getErrorCode() {
        return errorHolder.getCode();
    }

    public ErrorHolder getErrorHolder() {
        return errorHolder;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
