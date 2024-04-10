package com.spribe.exchanger.repository;

import com.spribe.exchanger.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long>, JpaSpecificationExecutor<CurrencyEntity> {
    Optional<CurrencyEntity> findByCode(String code);
}
