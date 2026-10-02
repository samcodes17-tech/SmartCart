package com.smartcart.service;

import com.smartcart.dto.InventorySettingsForm;
import com.smartcart.entity.*;
import com.smartcart.repository.InventoryRepository;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.StockTransactionRepository;
import com.smartcart.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final EmailService emailService;

    public InventoryService(InventoryRepository inventoryRepository,
                             ProductRepository productRepository,
                             SupplierRepository supplierRepository,
                             StockTransactionRepository stockTransactionRepository,
                             EmailService emailService) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.emailService = emailService;
    }

    public void createDefaultInventory(Product product) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(0);
        inventory.setReorderLevel(10);
        inventoryRepository.save(inventory);
    }


    public void syncMissingInventory() {
        List<Product> allProducts = productRepository.findAll();
        for (Product product : allProducts) {
            if (!inventoryRepository.existsByProductId(product.getId())) {
                createDefaultInventory(product);
            }
        }
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }


    @Transactional
    public void deleteByProductId(Long productId) {
        inventoryRepository.findByProductId(productId).ifPresent(inventory -> {
            stockTransactionRepository.deleteByInventoryId(inventory.getId());
            inventoryRepository.delete(inventory);
        });
    }


    public java.util.Map<Long, Integer> quantityByProductId() {
        java.util.Map<Long, Integer> map = new java.util.HashMap<>();
        for (Inventory inv : inventoryRepository.findAll()) {
            if (inv.getProduct() != null) {
                map.put(inv.getProduct().getId(), inv.getQuantityInStock());
            }
        }
        return map;
    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Inventory record not found"));
    }

    public List<Inventory> findLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<StockTransaction> findHistory(Long inventoryId) {
        return stockTransactionRepository.findByInventoryIdOrderByTransactionDateDesc(inventoryId);
    }

    public void updateSettings(Long id, InventorySettingsForm form) {
        Inventory inventory = findById(id);
        inventory.setReorderLevel(form.getReorderLevel());

        if (form.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(form.getSupplierId())
                    .orElseThrow(() -> new NoSuchElementException("Supplier not found"));
            inventory.setSupplier(supplier);
        } else {
            inventory.setSupplier(null);
        }

        inventoryRepository.save(inventory);
    }

    @Transactional
    public void stockIn(Long id, int quantity) {
        stockIn(id, quantity, "Manual stock-in by admin");
    }

    @Transactional
    public void stockIn(Long id, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Inventory inventory = findById(id);
        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        inventory.setLastRestockedDate(LocalDateTime.now());
        inventoryRepository.save(inventory);

        logTransaction(inventory, TransactionType.STOCK_IN, quantity, note);
    }

    @Transactional
    public void stockOut(Long id, int quantity) {
        stockOut(id, quantity, "Manual stock-out by admin");
    }

    @Transactional
    public void stockOut(Long id, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Inventory inventory = findById(id);
        if (quantity > inventory.getQuantityInStock()) {
            throw new IllegalArgumentException("Cannot remove more than the current stock (" + inventory.getQuantityInStock() + " available)");
        }

        boolean wasLowStock = inventory.isLowStock();

        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventoryRepository.save(inventory);

        logTransaction(inventory, TransactionType.STOCK_OUT, quantity, note);

        if (inventory.isLowStock() && !wasLowStock) {
            emailService.sendLowStockAlert(inventory);
        }
    }

    private void logTransaction(Inventory inventory, TransactionType type, int quantity, String note) {
        StockTransaction transaction = new StockTransaction();
        transaction.setInventory(inventory);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setNote(note);
        stockTransactionRepository.save(transaction);
    }
}
