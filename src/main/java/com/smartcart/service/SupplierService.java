package com.smartcart.service;

import com.smartcart.entity.Supplier;
import com.smartcart.repository.InventoryRepository;
import com.smartcart.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;

    public SupplierService(SupplierRepository supplierRepository, InventoryRepository inventoryRepository) {
        this.supplierRepository = supplierRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found"));
    }

    public void save(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        if (inventoryRepository.existsBySupplierId(id)) {
            throw new IllegalStateException("Cannot delete a supplier that is still assigned to inventory records");
        }
        supplierRepository.deleteById(id);
    }
}
