package com.haminton.product.repository;

import com.haminton.product.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepositoy extends JpaRepository<ProductEntity, Long> {
}
