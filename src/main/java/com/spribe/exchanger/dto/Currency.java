package com.spribe.exchanger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.spribe.exchanger.utill.PageResponse.Pagination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Currency {

    @NotBlank
    @Size(max = 100)
    @JsonView({OnAddNewCurrency.class, OnGetAllCurrencies.class, OnGetAllCurrencyRates.class})
    private String name;

    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "[A-Z]+")
    @JsonView({OnAddNewCurrency.class, OnGetAllCurrencies.class, OnGetAllCurrencyRates.class})
    private String code;

    @JsonView({OnGetAllCurrencyRates.class})
    private Set<Rate> rates;


    public interface OnGetAllCurrencies extends Pagination {
    }

    public interface OnGetAllCurrencyRates extends Pagination {
    }

    public interface OnAddNewCurrency {
    }

}

