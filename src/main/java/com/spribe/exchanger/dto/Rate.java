package com.spribe.exchanger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;

import java.math.BigDecimal;

import static com.spribe.exchanger.dto.Currency.OnGetAllCurrencyRates;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Rate(
        @JsonView({OnGetAllCurrencyRates.class}) String currencyCode,
        @JsonView({OnGetAllCurrencyRates.class}) BigDecimal rateValue) {
}