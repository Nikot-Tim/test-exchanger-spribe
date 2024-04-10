package com.spribe.exchanger.integration;

import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.dto.Rate;

import java.util.Set;

public interface ExchangerClient {
    Set<Currency> fetchCurrencies();

    Set<Rate> fetchRatesForCurrency(String baseCurrency);
}
