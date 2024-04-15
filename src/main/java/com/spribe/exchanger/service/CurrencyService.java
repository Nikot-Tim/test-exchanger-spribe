package com.spribe.exchanger.service;

import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.utill.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CurrencyService {

    void updateAvailableCurrencies();

    void refreshCurrenciesRates();

    Currency addCurrency(Currency currency);

    Currency getCurrency(String code);

    Currency getAvailableForUseCurrencyByCode(String code);

    PageResponse<Currency> getAllCurrencies(Pageable pageable);

}
