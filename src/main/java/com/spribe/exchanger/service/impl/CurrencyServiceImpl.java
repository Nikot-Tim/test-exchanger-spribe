package com.spribe.exchanger.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.dto.Rate;
import com.spribe.exchanger.entity.CurrencyEntity;
import com.spribe.exchanger.error.ErrorCode;
import com.spribe.exchanger.exception.BusinessException;
import com.spribe.exchanger.integration.ExchangerClient;
import com.spribe.exchanger.mapper.CurrencyMapper;
import com.spribe.exchanger.repository.CurrencyRepository;
import com.spribe.exchanger.service.CurrencyService;
import com.spribe.exchanger.utill.PageResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {


    @Value("${application.currency.availableCurrenciesFilePath}")
    private String availableCurrenciesFilePath;

    private final ExchangerClient exchangerClient;

    private final CurrencyMapper currencyMapper;

    private final CurrencyRepository currencyRepository;
    private final Map<String, Currency> memoryRatesDb = new HashMap<>();

    @Override
    @PostConstruct
    public void updateAvailableCurrencies() {
        Set<Currency> currencies = exchangerClient.fetchCurrencies();
        writeCurrenciesToFile(currencies);
    }

    @Override
    @PostConstruct
    public void refreshCurrenciesRates() {
        memoryRatesDb.clear();

        currencyRepository.findAll()
                .forEach(savedCurrency -> {
                    var refreshedCurrencyRates = exchangerClient.fetchRatesForCurrency(savedCurrency.getCode());
                    savedCurrency.setRates(refreshedCurrencyRates);
                    var refreshedCurrency = currencyMapper.toDto(currencyRepository.save(savedCurrency));
                    memoryRatesDb.put(refreshedCurrency.getCode(), refreshedCurrency);
                });
    }

    @Override
    public Currency getAvailableForUseCurrencyByCode(String currencyCode) {
        Set<Currency> currencies = readCurrenciesFromFile();
        if (currencies != null) {
            for (Currency currency : currencies) {
                if (currency.getCode().equals(currencyCode)) {
                    return currency;
                }
            }
        }
        return null;
    }

    private void writeCurrenciesToFile(Set<Currency> currencies) {
        try (FileWriter writer = new FileWriter(availableCurrenciesFilePath)) {
            // Serialize currencies to JSON and write to file
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, currencies);
        } catch (IOException e) {
            log.error("Error writing currencies to file: {}", e.getMessage());
        }
    }

    private Set<Currency> readCurrenciesFromFile() {
        try (FileReader reader = new FileReader(availableCurrenciesFilePath)) {
            // Deserialize currencies from JSON file
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(reader, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Error reading currencies from file: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Currency addCurrency(Currency currency) {
        checkIfCurrencyAlreadyExistsByCode(currency.getCode());

        Set<Rate> rates = exchangerClient.fetchRatesForCurrency(currency.getCode());
        currency.setRates(rates);

        var currencyEntity = currencyMapper.toEntity(currency);
        var savedCurrencyEntity = currencyRepository.save(currencyEntity);
        var savedCurrency = currencyMapper.toDto(savedCurrencyEntity);

        memoryRatesDb.put(savedCurrency.getCode(), savedCurrency);

        return savedCurrency;
    }

    private void checkIfCurrencyAlreadyExistsByCode(String code) {
        Currency currency = memoryRatesDb.get(code);
        if (currency != null || currencyRepository.findByCode(code).isPresent()) {
            log.info("Currency with code = {} already exists", code);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.CURRENCY_ALREADY_EXISTS.withAdditionalParam(code)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Currency getCurrency(String code) {
        log.info("CurrencyServiceImpl.getCurrency fetching data from memory started");
        var currency = memoryRatesDb.get(code);
        log.info("CurrencyServiceImpl.getCurrency fetching data from memory finished");

        if (currency == null) {
            log.info("CurrencyServiceImpl.getCurrency fetching data from database started");
            currency = currencyRepository.findByCode(code)
                    .map(currencyMapper::toDto)
                    .orElseThrow(() ->
                            new BusinessException(
                                    HttpStatus.NOT_FOUND,
                                    ErrorCode.CAN_NOT_FIND_CURRENCY_BY_CODE.withAdditionalParam(code)
                            )
                    );
            memoryRatesDb.put(currency.getCode(), currency);
            log.info("CurrencyServiceImpl.getCurrency fetching data from database finished");
        }
        return currency;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Currency> getAllCurrencies(Pageable pageable) {
        log.info("CurrencyServiceImpl.getAllCurrencies fetching data from memory started");
        var currencies = memoryRatesDb.values().stream().toList();
        log.info("CurrencyServiceImpl.getAllCurrencies fetching data from memory finished");

        if (CollectionUtils.isEmpty(currencies)) {
            log.info("CurrencyServiceImpl.getAllCurrencies fetching data from database started");
            List<CurrencyEntity> currencyEntities = currencyRepository.findAll();
            currencies = currencyMapper.toDto(currencyEntities);
            log.info("CurrencyServiceImpl.getAllCurrencies fetching data from database finished");
        }

        return PageResponse.of(getCurrenciesForPage(currencies, pageable), pageable, (long) currencies.size());
    }

    private List<Currency> getCurrenciesForPage(List<Currency> currencies, Pageable pageable) {
        int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
        int toIndex = fromIndex + pageable.getPageSize();

        int maxSize = currencies.size();

        if (maxSize < fromIndex) {
            return Collections.emptyList();
        }

        if (maxSize < toIndex) {
            toIndex = currencies.size();
        }

        return currencies.stream()
                .toList().subList(fromIndex, toIndex);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public PageResponse<Currency> getAllCurrencies(Specification specification, Pageable pageable) {
//        log.info("CurrencyServiceImpl.getAllCurrencies fetching data from memory started");
//        var currencies = memoryRatesDb.values();
//        log.info("CurrencyServiceImpl.getAllCurrencies fetching data from memory finished");
//
//        if (CollectionUtils.isEmpty(currencies)) {
//            log.info("CurrencyServiceImpl.getAllCurrencies fetching data from database started");
//            Page<CurrencyEntity> currencyEntities = currencyRepository.findAll(specification, pageable);
//            currencies = currencyMapper.toDto(currencyEntities.getContent());
//            log.info("CurrencyServiceImpl.getAllCurrencies fetching data from database finished");
//        }
//
//        return PageResponse.of(currencies, pageable, currencies.getTotalElements());
//    }
}
