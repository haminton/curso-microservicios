package com.haminton.product.controller;


import com.haminton.product.entities.ProductEntity;
import com.haminton.product.repository.ProductRepositoy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired(required=true)
    ProductRepositoy productRepository;

    @GetMapping()
    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ProductEntity findById(@PathVariable Long id) {
        return productRepository.findById(id).get();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductEntity> put(@PathVariable Long id, @RequestBody ProductEntity request) {
        ProductEntity product = productRepository.findById(id).get();
        if (Objects.nonNull(product)) {
            product.setName(request.getName());
            product.setCode(request.getCode());
        }
        ProductEntity save = productRepository.saveAndFlush(product);
        return ResponseEntity.ok(save);
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody ProductEntity request) {
        ProductEntity save = productRepository.save(request);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<ProductEntity> customer = productRepository.findById(id);
        if (Objects.nonNull(customer.get())) {
            productRepository.delete(customer.get());
        }
        return ResponseEntity.ok().build();
    }



}
