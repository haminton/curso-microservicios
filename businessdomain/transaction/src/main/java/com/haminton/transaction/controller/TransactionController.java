package com.haminton.transaction.controller;


import com.haminton.transaction.entities.Status;
import com.haminton.transaction.entities.TransactionEntity;
import com.haminton.transaction.repository.TransactionRepositoy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired(required=true)
    TransactionRepositoy transactionRepository;

    @GetMapping()
    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    @GetMapping("/{accountIban}")
    public ResponseEntity<?> findById(@PathVariable String accountIban) {
        List<TransactionEntity> transaction = transactionRepository.findByAccountIban(accountIban);
        double balance = transaction.stream().mapToDouble(TransactionEntity::getAmount).sum();
        Map<String, Object> response = new HashMap<>();
        response.put("Balance", balance);
        response.put("transactions", transaction);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionEntity> put(@PathVariable Long id, @RequestBody TransactionEntity request) {
        TransactionEntity transaction = transactionRepository.findById(id).get();
        if (Objects.nonNull(transaction)) {
            transaction.setAmount(request.getAmount());
            transaction.setStatus(request.getStatus());
            transaction.setDate(request.getDate());
            transaction.setFee(request.getFee());
            transaction.setChannel(request.getChannel());
            transaction.setDescription(request.getDescription());
            transaction.setAccountIban(request.getAccountIban());
        }
        TransactionEntity save = transactionRepository.saveAndFlush(transaction);
        return ResponseEntity.ok(save);
    }

    @PostMapping
    public ResponseEntity<?> post(@Valid @RequestBody TransactionEntity request, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(err -> "El campo " + err.getField() + " : " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        if(request.getAmount() == 0) {
            return new ResponseEntity<>("El valor no puede ser cero", HttpStatus.BAD_REQUEST);
        }
        if (request.getFee() > 0) {
            request.setAmount(request.getAmount() - request.getFee());
        }


        LocalDate date = LocalDate.now();
        if (date.isBefore(request.getDate())) {
            request.setStatus(Status.STATUS_01);
        } else if (date.isAfter(request.getDate()) || date.isEqual(request.getDate())) {
            request.setStatus(Status.STATUS_02);
        }
        TransactionEntity save = transactionRepository.save(request);
        return ResponseEntity.ok(save);
    }

}
