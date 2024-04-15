package com.spribe.exchanger.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.entity.CurrencyEntity;
import com.spribe.exchanger.exception.BusinessException;
import com.spribe.exchanger.mapper.CurrencyMapper;
import com.spribe.exchanger.service.CurrencyService;
import com.spribe.exchanger.utill.PageResponse;
import com.spribe.exchanger.utils.TestUtils;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.spribe.exchanger.error.ErrorCode.CAN_NOT_FIND_CURRENCY_BY_CODE;
import static com.spribe.exchanger.error.ErrorCode.CURRENCY_CODE_IS_NOT_AVAILABLE_FOR_USE;
import static com.spribe.exchanger.error.ErrorCode.CURRENCY_NAME_IS_NOT_VALID;
import static com.spribe.exchanger.error.ErrorCode.REQUEST_NOT_VALID;
import static com.spribe.exchanger.utils.TestUtils.TEST_CURRENCY_CODE;
import static com.spribe.exchanger.utils.TestUtils.TEST_CURRENCY_NAME;
import static com.spribe.exchanger.utils.TestUtils.asJsonString;
import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CurrencyControllerTest {
    private static final String TEST_CURRENCY_CODE_FIELD = "code";
    private static final String TEST_CURRENCY_NAME_FIELD = "name";
    private static final String CURRENCY_ENDPOINT = "/currency";
    private static final String GET_CURRENCY_BY_CODE_URL_PATH = "/%s/rates";

    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private CurrencyMapper currencyMapper;
    @MockBean
    private CurrencyService currencyService;

    @Test
    public void testGetAllCurrenciesUsedInProject() throws Exception {
        List<CurrencyEntity> currencyEntities = TestUtils.getMockData("currency_entity_list", new TypeReference<>() {
        });
        List<Currency> currencies = currencyMapper.toDto(currencyEntities);

        PageResponse<Currency> pageResponse = new PageResponse<>(
                currencies, PageRequest.of(0, 10), (long) currencies.size()
        );

        given(currencyService.getAllCurrencies(any(Pageable.class))).willReturn(pageResponse);

        mockMvc.perform(get(CURRENCY_ENDPOINT)
                        .param("currentPage", "0")
                        .param("perPage", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalRecords").value(5));

        verify(currencyService, times(1)).getAllCurrencies(any(Pageable.class));
    }

    @Test
    public void testGetCurrencyRates() throws Exception {
        CurrencyEntity currencyEntity = TestUtils.getMockData("currency_entity", new TypeReference<>() {
        });
        Currency currency = currencyMapper.toDto(currencyEntity);

        String code = currency.getCode();
        given(currencyService.getCurrency(code)).willReturn(currency);

        mockMvc.perform(get(format(CURRENCY_ENDPOINT + format(GET_CURRENCY_BY_CODE_URL_PATH, code)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.name").value(currency.getName()))
                .andExpect(jsonPath("$.rates").isNotEmpty());

        verify(currencyService, times(1)).getCurrency(code);
    }

    @Test
    public void testGetCurrencyRatesNotFound() throws Exception {
        given(currencyService.getCurrency(TEST_CURRENCY_CODE)).willThrow(
                new BusinessException(
                        HttpStatus.NOT_FOUND,
                        CAN_NOT_FIND_CURRENCY_BY_CODE.withAdditionalParam(TEST_CURRENCY_CODE)
                ));

        mockMvc.perform(get(format(CURRENCY_ENDPOINT + format(GET_CURRENCY_BY_CODE_URL_PATH, TEST_CURRENCY_CODE)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(CAN_NOT_FIND_CURRENCY_BY_CODE.name()))
                .andExpect(jsonPath("$.message").value(containsString(TEST_CURRENCY_CODE)));

        verify(currencyService, times(1)).getCurrency(TEST_CURRENCY_CODE);
    }

    @Test
    public void testAddCurrencyCodeIsNotValid() throws Exception {
        //Must not be null
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_CODE_FIELD));


        //Must not be blank
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code("")
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_CODE_FIELD));

        //Wrong case
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE.toLowerCase())
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_CODE_FIELD));

        //Wrong size more than 3
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE + "USD")
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_CODE_FIELD));

        //Wrong size less than 3
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code("US")
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_CODE_FIELD));

        given(currencyService.getAvailableForUseCurrencyByCode(TEST_CURRENCY_CODE)).willReturn(null);

        //Not available currency code
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .name(TEST_CURRENCY_NAME)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(CURRENCY_CODE_IS_NOT_AVAILABLE_FOR_USE.name()))
                .andExpect(jsonPath("$.message").value(containsString(TEST_CURRENCY_CODE)));
    }

    @Test
    public void testAddCurrencyNameIsNotValid() throws Exception {
        //Must not be null
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_NAME_FIELD));


        //Must not be blank
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .name("")
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_NAME_FIELD));

        //Wrong size more than 100
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .name(RandomString.make(101))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(REQUEST_NOT_VALID.name()))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.validationDetails").exists())
                .andExpect(jsonPath("$.details.validationDetails[0].field").value(TEST_CURRENCY_NAME_FIELD));

        CurrencyEntity currencyEntity = TestUtils.getMockData("currency_entity", new TypeReference<>() {
        });

        Currency currency = currencyMapper.toDto(currencyEntity);

        given(currencyService.getAvailableForUseCurrencyByCode(TEST_CURRENCY_CODE)).willReturn(currency);

        String testCurrencyName = TEST_CURRENCY_NAME + "test";

        //Not valid currency name
        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(Currency.builder()
                                .code(TEST_CURRENCY_CODE)
                                .name(testCurrencyName)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(CURRENCY_NAME_IS_NOT_VALID.name()))
                .andExpect(jsonPath("$.message").value(containsString(testCurrencyName)));
    }

    @Test
    public void testAddCurrency() throws Exception {
        CurrencyEntity currencyEntity = TestUtils.getMockData("currency_entity", new TypeReference<>() {
        });
        Currency currency = currencyMapper.toDto(currencyEntity);

        Currency createCurrencyRequest = Currency.builder()
                .code(TEST_CURRENCY_CODE)
                .name(TEST_CURRENCY_NAME)
                .build();

        given(currencyService.getAvailableForUseCurrencyByCode(TEST_CURRENCY_CODE)).willReturn(currency);
        given(currencyService.addCurrency(any(Currency.class))).willReturn(currency);

        mockMvc.perform(post(CURRENCY_ENDPOINT)
                        .content(asJsonString(createCurrencyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.code").value(currency.getCode()))
                .andExpect(jsonPath("$.name").value(currency.getName()))
                .andExpect(jsonPath("$.rates").isNotEmpty());
    }
}
