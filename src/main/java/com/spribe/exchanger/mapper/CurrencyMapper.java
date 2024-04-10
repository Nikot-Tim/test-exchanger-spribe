package com.spribe.exchanger.mapper;

import com.spribe.exchanger.dto.Currency;
import com.spribe.exchanger.entity.CurrencyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    Currency toDto(CurrencyEntity source);

    List<Currency> toDto(List<CurrencyEntity> source);

    @Mapping(target = "id", ignore = true)
    CurrencyEntity toEntity(Currency source);
}
