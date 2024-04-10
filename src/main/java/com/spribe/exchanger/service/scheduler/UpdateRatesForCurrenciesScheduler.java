package com.spribe.exchanger.service.scheduler;

import com.spribe.exchanger.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRatesForCurrenciesScheduler {

    private final CurrencyService currencyService;

    @Scheduled(cron = "${scheduler.updateRatesForCurrenciesScheduler.cronExpression}")
    public void updateRatesForCurrenciesScheduler() {
        log.info("UpdateRatesForCurrenciesScheduler is start working");
        currencyService.refreshCurrenciesRates();
        log.info("UpdateRatesForCurrenciesScheduler updated currencies rates successfully");
    }
}
