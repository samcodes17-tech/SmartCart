package com.smartcart.repository;

import com.smartcart.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    boolean existsBySupplierId(Long supplierId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock <= i.reorderLevel")
    List<Inventory> findLowStockItems();
}
