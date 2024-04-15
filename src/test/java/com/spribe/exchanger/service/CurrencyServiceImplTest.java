package com.spribe.exchanger.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.dto.Rate;
import com.spribe.exchanger.entity.CurrencyEntity;
import com.spribe.exchanger.error.ErrorCode;
import com.spribe.exchanger.exception.BusinessException;
import com.spribe.exchanger.integration.ExchangerClient;
import com.spribe.exchanger.mapper.CurrencyMapper;
import com.spribe.exchanger.repository.CurrencyRepository;
import com.spribe.exchanger.service.impl.CurrencyServiceImpl;
import com.spribe.exchanger.utill.PageResponse;
import com.spribe.exchanger.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.spribe.exchanger.utils.TestUtils.TEST_CURRENCY_CODE;
import static com.spribe.exchanger.utils.TestUtils.TEST_CURRENCY_NAME;
import static java.lang.Integer.MAX_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceImplTest {

    private static final String AVAILABLE_CURRENCIES_TEST_FILE_PATH = "currencies_test.json";
    private CurrencyMapper currencyMapper;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ExchangerClient exchangerClient;
    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        currencyMapper = Mappers.getMapper(CurrencyMapper.class);

        ReflectionTestUtils.setField(currencyService, "currencyMapper", currencyMapper);
        ReflectionTestUtils.setField(currencyService, "availableCurrenciesFilePath", AVAILABLE_CURRENCIES_TEST_FILE_PATH);
    }

    @AfterEach
    void cleanUp() {
        ReflectionTestUtils.invokeMethod(currencyService, "destroy");
    }

    @Test
    void updateAvailableCurrencies() {
        List<CurrencyEntity> currencyEntities = TestUtils.getMockData("currency_entity_list", new TypeReference<>() {
        });
        List<Currency> currencies = currencyMapper.toDto(currencyEntities);

        when(exchangerClient.fetchCurrencies()).thenReturn(new HashSet<>(currencies));
        currencyService.updateAvailableCurrencies();
        Currency availableCurrency = currencyService.getAvailableForUseCurrencyByCode(TEST_CURRENCY_CODE);
        assertNotNull(availableCurrency);
    }

    @Test
    void refreshCurrenciesRates() {
        List<CurrencyEntity> currencyEntities = TestUtils.getMockData("currency_entity_list", new TypeReference<>() {
        });

        CurrencyEntity currencyEntity = TestUtils.getMockData("currency_entity", new TypeReference<>() {
        });

        Set<Rate> rateSet = currencyEntity.getRates();

        when(currencyRepository.findAll()).thenReturn(currencyEntities);
        when(exchangerClient.fetchRatesForCurrency(anyString())).thenReturn(rateSet);

        currencyService.refreshCurrenciesRates();

        verify(currencyRepository, times(1)).findAll();
        verify(exchangerClient, times(currencyEntities.size())).fetchRatesForCurrency(anyString());
        verify(currencyRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getAvailableForUseCurrencyByCodeReturnNull() {
        Currency availableCurrency = currencyService.getAvailableForUseCurrencyByCode(TEST_CURRENCY_CODE);
        assertNull(availableCurrency);
    }

    @Test
    void addCurrency() {
        CurrencyEntity currencyEntity = TestUtils.getMockData("currency_entity", new TypeReference<>() {
        });

        Set<Rate> rateSet = currencyEntity.getRates();

        when(exchangerClient.fetchRatesForCurrency(anyString())).thenReturn(rateSet);

        when(currencyRepository.save(any(CurrencyEntity.class))).thenReturn(currencyEntity);

        Currency result = currencyService.addCurrency(
                Currency.builder()
                        .code(TEST_CURRENCY_CODE)
                        .name(TEST_CURRENCY_NAME)
                        .build());

        assertNotNull(result);
        verify(currencyRepository, times(1)).findByCode(TEST_CURRENCY_CODE);
        verify(exchangerClient, times(1)).fetchRatesForCurrency(TEST_CURRENCY_CODE);
        verify(currencyRepository, times(1)).save(any(CurrencyEntity.class));
    }

    @Test
    void addCurrencyAlreadyExistsError() {
        addCurrency();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> currencyService.addCurrency(
                        Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .name(TEST_CURRENCY_NAME)
                                .build())
        );

        assertThat(exception.getHttpStatus(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertTrue(exception.getErrorHolder().getMessage()
                .contains(ErrorCode.CURRENCY_ALREADY_EXISTS.getMessage()));
        verifyNoMoreInteractions(currencyRepository);
        verifyNoMoreInteractions(exchangerClient);
    }

    @Test
    void getCurrency() {
        addCurrency();

        currencyService.getCurrency(TEST_CURRENCY_CODE);

        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void getCurrencyNotFoundException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> currencyService.getCurrency(TEST_CURRENCY_CODE)
        );

        assertThat(exception.getHttpStatus(), is(equalTo(HttpStatus.NOT_FOUND)));
        assertTrue(exception.getErrorHolder().getMessage()
                .contains(ErrorCode.CAN_NOT_FIND_CURRENCY_BY_CODE.getMessage()));

        verify(currencyRepository, times(1)).findByCode(TEST_CURRENCY_CODE);
    }

    @ParameterizedTest
    @MethodSource("pageArguments")
    void getAllCurrencies(int currentPage, int perPage, int expectedContentSize) {

        List<CurrencyEntity> currencyEntities = TestUtils.getMockData("currency_entity_list", new TypeReference<>() {
        });

        when(currencyRepository.findAll()).thenReturn(currencyEntities);

        Pageable pageable = PageRequest.of(currentPage, perPage);

        PageResponse<Currency> result = currencyService.getAllCurrencies(pageable);

        assertNotNull(result);
        List<Currency> content = result.getContent();
        assertNotNull(content);
        assertThat(content, hasSize(expectedContentSize));
        assertThat(result.getTotalRecords(), is((long) currencyEntities.size()));
    }

    private static Stream<Arguments> pageArguments() {
        return Stream.of(
                Arguments.of(0, MAX_VALUE, 5),
                Arguments.of(1, 2, 2),
                Arguments.of(2, 2, 1),
                Arguments.of(3, 2, 0)
        );
    }
}

