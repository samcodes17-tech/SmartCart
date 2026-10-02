package com.smartcart.repository;

import com.smartcart.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByInventoryIdOrderByTransactionDateDesc(Long inventoryId);

    void deleteByInventoryId(Long inventoryId);
}
