package com.spribe.exchanger.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorHolder {

    private ErrorCode code;
    private List<String> args;
    private String message;

    public String getMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        } else {
            return code.getMessage();
        }
    }
}