package com.spribe.exchanger.utill.validation;

import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.error.ErrorCode;
import com.spribe.exchanger.exception.BusinessException;
import com.spribe.exchanger.service.CurrencyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, Currency> {

    private final CurrencyService currencyService;

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Currency currency, ConstraintValidatorContext context) {
        if (currency == null) {
            return false;
        }

        var code = currency.getCode();
        var name = currency.getName();
        Currency availableCurrency = currencyService.getAvailableForUseCurrencyByCode(code);

        if (availableCurrency == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CURRENCY_CODE_IS_NOT_AVAILABLE_FOR_USE.withAdditionalParam(code)
            );
        } else if (!availableCurrency.getName().equalsIgnoreCase(name)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CURRENCY_NAME_IS_NOT_VALID.withAdditionalParam(name));
        }

        return true;
    }
}
