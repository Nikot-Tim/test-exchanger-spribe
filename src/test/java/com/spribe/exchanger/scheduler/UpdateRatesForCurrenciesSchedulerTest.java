package com.spribe.exchanger.scheduler;

import com.spribe.exchanger.service.CurrencyService;
import com.spribe.exchanger.service.scheduler.UpdateRatesForCurrenciesScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateRatesForCurrenciesSchedulerTest {
    @Mock
    private CurrencyService currencyService;
    @InjectMocks
    private UpdateRatesForCurrenciesScheduler updateRatesForCurrenciesScheduler;

    @Test
    public void shouldUpdateRatesForCurrencies() {
        doNothing().when(currencyService).refreshCurrenciesRates();
        updateRatesForCurrenciesScheduler.updateRatesForCurrenciesScheduler();
        verify(currencyService, times(1)).refreshCurrenciesRates();
    }
}
