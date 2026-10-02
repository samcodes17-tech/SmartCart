package com.smartcart.dto;

import com.smartcart.entity.Product;
import lombok.Getter;

/**
 * Combines a Product with its current stock status for display purposes only.
 * Keeping this assembly in the controller/service layer — rather than calling
 * repository methods directly from the Thymeleaf template — keeps business
 * logic out of the view, consistent with the layered architecture used
 * throughout the rest of the project.
 */
@Getter
public class ProductCatalogItem {
    private final Product product;
    private final int quantityInStock;

    public ProductCatalogItem(Product product, int quantityInStock) {
        this.product = product;
        this.quantityInStock = quantityInStock;
    }

    public boolean isInStock() {
        return quantityInStock > 0;
    }
}
