package com.haminton.customer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransaction implements Serializable {

    private Long id;
    private String reference;
    private String accountIban;
    private LocalDate date;

    private double amount;

    private double fee;
    private String description;
    private String status;
    private String channel;

    private static final long serialVersionUID = 4155118557911956629L;


}
