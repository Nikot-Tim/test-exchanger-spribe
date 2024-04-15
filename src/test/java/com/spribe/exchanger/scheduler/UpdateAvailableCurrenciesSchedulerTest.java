package com.spribe.exchanger.scheduler;

import com.spribe.exchanger.service.CurrencyService;
import com.spribe.exchanger.service.scheduler.UpdateAvailableCurrenciesScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateAvailableCurrenciesSchedulerTest {

    @Mock
    private CurrencyService currencyService;
    @InjectMocks
    private UpdateAvailableCurrenciesScheduler updateAvailableCurrenciesScheduler;

    @Test
    public void shouldUpdateAvailableCurrencies() {
        doNothing().when(currencyService).updateAvailableCurrencies();
        updateAvailableCurrenciesScheduler.updateAvailableCurrenciesScheduler();
        verify(currencyService, times(1)).updateAvailableCurrencies();
    }
}
