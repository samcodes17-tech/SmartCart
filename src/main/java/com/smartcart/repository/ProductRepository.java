package com.smartcart.repository;

import com.smartcart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    List<Product> findByCategoryId(Long categoryId);
}
