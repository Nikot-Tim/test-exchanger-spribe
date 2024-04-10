package com.spribe.exchanger.error;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ErrorCode {
    REQUEST_NOT_VALID("error.request.not.valid"),
    ERROR_CAN_NOT_FETCH_AVAILABLE_CURRENCIES("error.can.not.fetch.available.currencies"),
    CAN_NOT_FIND_CURRENCY_BY_CODE("error.can.not.find.currency.by.code"),
    CURRENCY_ALREADY_EXISTS("error.currency.already.exists"),
    CURRENCY_CODE_IS_NOT_AVAILABLE_FOR_USE("error.currency.code.is.not.available.for.use"),
    CURRENCY_NAME_IS_NOT_VALID("error.currency.name.is.not.valid");


    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public ErrorHolder withAdditionalParam(String... params) {
        return ErrorHolder.builder()
                .code(this)
                .args(Arrays.asList(params))
                .build();
    }

    public ErrorHolder code() {
        return ErrorHolder.builder()
                .code(this)
                .build();
    }
}
