package com.haminton.customer.repository;

import com.haminton.customer.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepositoy extends JpaRepository<CustomerEntity, Long> {

    @Query("SELECT c FROM CustomerEntity c WHERE c.code = ?1")
    public CustomerEntity findByCode(String code);

    @Query("SELECT c FROM CustomerEntity c WHERE c.iban = ?1")
    public CustomerEntity findByAccount(String iban);
}
