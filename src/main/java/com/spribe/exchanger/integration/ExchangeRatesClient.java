package com.spribe.exchanger.integration;

import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.dto.Rate;
import com.spribe.exchanger.dto.exchangerates.AvailableCurrenciesResponse;
import com.spribe.exchanger.dto.exchangerates.CurrencyRatesResponse;
import com.spribe.exchanger.error.ErrorCode;
import com.spribe.exchanger.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRatesClient implements ExchangerClient {

    @Value("${exchanger.baseUrl}")
    private String baseUrl;
    @Value("${exchanger.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String API_KEY = "access_key";
    private static final String GET_AVAILABLE_CURRENCIES_PATH = "/symbols";
    private static final String GET_BASE_CURRENCY_RATES_PATH = "/latest";

    @Override
    public Set<Currency> fetchCurrencies() {
        UriComponents uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(GET_AVAILABLE_CURRENCIES_PATH)
                .queryParam(API_KEY, apiKey)
                .build();

        ResponseEntity<AvailableCurrenciesResponse> availableCurrencies =
                restTemplate.getForEntity(uri.toUri(), AvailableCurrenciesResponse.class);

        if (availableCurrencies.getStatusCode().is2xxSuccessful()) {
            var body = availableCurrencies.getBody();
            if (body != null) {
                return body.symbols().entrySet()
                        .stream()
                        .map(entry -> Currency.builder()
                                .code(entry.getKey())
                                .name(entry.getValue())
                                .build())
                        .collect(Collectors.toSet());
            }
        }
        log.error("Error fetching available currencies");
        throw new BusinessException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.ERROR_CAN_NOT_FETCH_AVAILABLE_CURRENCIES.code()
        );
    }

    @Override
    public Set<Rate> fetchRatesForCurrency(String baseCurrency) {
        UriComponents uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(GET_BASE_CURRENCY_RATES_PATH)
                .queryParam(API_KEY, apiKey)
                .build();

        ResponseEntity<CurrencyRatesResponse> currencyRatesResponse =
                restTemplate.getForEntity(uri.toUri(), CurrencyRatesResponse.class);

        if (currencyRatesResponse.getStatusCode().is2xxSuccessful()) {
            var body = currencyRatesResponse.getBody();
            if (body != null) {

                return body.rates().entrySet()
                        .stream()
                        .map(entry -> Rate.builder()
                                .currencyCode(entry.getKey())
                                .rateValue(entry.getValue())
                                .build())
                        .collect(Collectors.toSet());
            }
        }

        log.error("Error fetching rates for currencies");
        return new HashSet<>();
    }
}
