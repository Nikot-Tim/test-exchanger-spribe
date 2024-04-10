package com.spribe.exchanger.dto.exchangerates;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record CurrencyRatesResponse(
        @JsonProperty("success") boolean success,
        @NotNull @JsonProperty("rates") Map<String, BigDecimal> rates
) {
}
