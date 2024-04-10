package com.spribe.exchanger.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.dto.Currency.OnAddNewCurrency;
import com.spribe.exchanger.dto.Currency.OnGetAllCurrencies;
import com.spribe.exchanger.service.CurrencyService;
import com.spribe.exchanger.utill.PageResponse;
import com.spribe.exchanger.utill.validation.ValidCurrency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.spribe.exchanger.utill.PageResponse.DEFAULT_PAGE_SIZE;
import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(path = "/currency", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Currency controller", description = "Operations related to currency")
public class CurrencyController {

    private final CurrencyService currencyService;

//    @JsonView({OnGetAllCurrencies.class})
//    @Operation(summary = "Get all currencies", description = "Retrieve a paginated list of all currencies")
//    @GetMapping
//    public ResponseEntity<PageResponse<Currency>> getAllCurrencies(
//            @Parameter(description = "Page number (default: 0)", example = "0") @RequestParam(defaultValue = "0") int currentPage,
//            @Parameter(description = "Items per page (default: 2147483647)", example = "10") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int perPage,
//            @Parameter(description = "Sort by field (default: code)", example = "code") @RequestParam(defaultValue = "code") String sortBy,
//            @Parameter(description = "Sort direction (default: ASC)", example = "ASC") @RequestParam(defaultValue = "ASC") String direction,
//            @Parameter(description = "Filter criteria") @RequestParam(required = false) String filter) {
//
//        Pageable pageable = PageRequest.of(currentPage, perPage);
//        Specification<String> specification = RequestSpecificationBuilder.buildSpecification(filter, sortBy, direction);
//        return ResponseEntity.of(ofNullable(currencyService.getAllCurrencies()));
//    }

    @JsonView({OnGetAllCurrencies.class})
    @Operation(summary = "Get all currencies used in project", description = "Retrieve a paginated list of all currencies")
    @GetMapping
    public ResponseEntity<PageResponse<Currency>> getAllCurrenciesUsedInProject(
            @Parameter(description = "Page number (default: 0)", example = "0") @RequestParam(defaultValue = "0") int currentPage,
            @Parameter(description = "Items per page (default: 2147483647)", example = "10") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int perPage
    ) {

        Pageable pageable = PageRequest.of(currentPage, perPage);
        return ResponseEntity.of(ofNullable(currencyService.getAllCurrencies(pageable)));
    }

    @Operation(summary = "Get exchange rates for a currency", description = "Retrieve the exchange rates for a specific currency")
    @GetMapping("{currencyCode}/rates")
    public ResponseEntity<Currency> getCurrencyRates(
            @Parameter(description = "Currency code", example = "USD") @PathVariable String currencyCode) {
        return ResponseEntity.of(ofNullable(currencyService.getCurrency(currencyCode)));
    }

    @Operation(summary = "Add a new currency", description = "Add a new currency")
    @PostMapping
    public ResponseEntity<Currency> addCurrency(
            @NotNull
            @Parameter(description = "Currency details")
            @ValidCurrency
            @RequestBody
            @JsonView(OnAddNewCurrency.class)
            Currency currency
    ) {
        return ResponseEntity.of(ofNullable(currencyService.addCurrency(currency)));
    }
}
