package com.smartcart.controller.shop;

import com.smartcart.dto.ProductCatalogItem;
import com.smartcart.entity.Product;
import com.smartcart.service.CategoryService;
import com.smartcart.service.InventoryService;
import com.smartcart.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ShopController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CategoryService categoryService;

    public ShopController(ProductService productService, InventoryService inventoryService, CategoryService categoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String browse(Model model) {
        Map<Long, Integer> stockByProduct = inventoryService.quantityByProductId();

        List<ProductCatalogItem> items = productService.findAll().stream()
                .map(p -> new ProductCatalogItem(p, stockByProduct.getOrDefault(p.getId(), 0)))
                .collect(Collectors.toList());

        model.addAttribute("items", items);
        model.addAttribute("categories", categoryService.findAll());
        return "shop/products";
    }
}
