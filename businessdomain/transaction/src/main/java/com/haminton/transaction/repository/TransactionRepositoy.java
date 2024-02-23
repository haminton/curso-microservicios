package com.haminton.transaction.repository;

import com.haminton.transaction.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepositoy extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT t FROM TransactionEntity t WHERE t.accountIban = ?1")
    public List<TransactionEntity> findByAccountIban(String accountIban);
}
