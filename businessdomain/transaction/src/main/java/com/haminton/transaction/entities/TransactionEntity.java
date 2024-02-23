package com.haminton.transaction.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Data
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String reference;
    private String accountIban;
    private LocalDate date;

    @NotBlank(message = "El campo 'amount' no puede estar en blanco")
    @Pattern(regexp = "^[+-]?([0-9]*[.])?[0-9]+$", message = "El valor debe ser un número positivo o negativo, excluyendo cero")
    private double amount;

    private double fee;
    private String description;
    private Status status;
    private Channel channel;

}

