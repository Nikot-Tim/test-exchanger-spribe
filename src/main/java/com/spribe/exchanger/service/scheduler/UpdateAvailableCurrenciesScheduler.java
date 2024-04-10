package com.spribe.exchanger.service.scheduler;

import com.spribe.exchanger.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateAvailableCurrenciesScheduler {

    private final CurrencyService currencyService;

    @Scheduled(cron = "${scheduler.updateAvailableCurrenciesScheduler.cronExpression}")
    public void updateAvailableCurrenciesScheduler() {
        log.info("UpdateAvailableCurrenciesScheduler is start working");
        currencyService.updateAvailableCurrencies();
        log.info("UpdateAvailableCurrenciesScheduler updated currencies successfully");
    }
}
