package com.spribe.exchanger.dto.exchangerates;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record AvailableCurrenciesResponse(boolean success, @NotEmpty Map<String, String> symbols) {
}


