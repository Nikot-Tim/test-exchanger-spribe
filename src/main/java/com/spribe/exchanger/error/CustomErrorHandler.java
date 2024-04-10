package com.spribe.exchanger.error;

import com.spribe.exchanger.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorHandler {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException exception) {
        List<String> args = Optional.ofNullable(exception)
                .map(BusinessException::getErrorHolder)
                .map(ErrorHolder::getArgs)
                .orElse(Collections.emptyList());

        String message = messageSource.getMessage(exception.getErrorHolder().getMessage(), args.toArray(), Locale.ENGLISH);

        return ResponseEntity
                .status(exception.getHttpStatus())
                .body(ErrorResponse.builder()
                        .message(message)
                        .code(exception.getErrorHolder().getCode())
                        .build()
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var result = ex.getBindingResult();
        List<ValidationDetail> validationDetails = result.getFieldErrors().stream()
                .map(fieldError -> ValidationDetail.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build()).collect(Collectors.toList());

        String message = messageSource.getMessage(ErrorCode.REQUEST_NOT_VALID.getMessage(), null, Locale.ENGLISH);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message(message)
                        .code(ErrorCode.REQUEST_NOT_VALID)
                        .details(Map.of("validationDetails", validationDetails))
                        .build()
                );
    }
}
