package com.smartcart.service;

import com.smartcart.dto.ProductForm;
import com.smartcart.entity.Category;
import com.smartcart.entity.Product;
import com.smartcart.repository.CartItemRepository;
import com.smartcart.repository.OrderItemRepository;
import com.smartcart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, CategoryService categoryService,
                           InventoryService inventoryService, CartItemRepository cartItemRepository,
                           OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public void createProduct(ProductForm form, String imageUrl) {
        if (productRepository.existsBySku(form.getSku())) {
            throw new IllegalArgumentException("A product with this SKU already exists");
        }

        Category category = categoryService.findById(form.getCategoryId());

        Product product = new Product();
        applyFormToProduct(form, product, category);
        product.setImageUrl(imageUrl);
        productRepository.save(product);
        inventoryService.createDefaultInventory(product);
    }

    public void updateProduct(Long id, ProductForm form, String newImageUrl) {
        if (productRepository.existsBySkuAndIdNot(form.getSku(), id)) {
            throw new IllegalArgumentException("A product with this SKU already exists");
        }

        Product product = findById(id);
        Category category = categoryService.findById(form.getCategoryId());
        applyFormToProduct(form, product, category);
        if (newImageUrl != null) {
            product.setImageUrl(newImageUrl);
        }
        productRepository.save(product);
    }


    @Transactional
    public void deleteProduct(Long id) {
        Product product = findById(id);


        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException(
                    "Cannot delete \"" + product.getName() + "\" because it has existing orders. " +
                    "Consider leaving it out of stock instead of deleting it.");
        }

        cartItemRepository.deleteByProductId(id);


        inventoryService.deleteByProductId(id);

        productRepository.deleteById(id);
    }

    public ProductForm toForm(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setSku(product.getSku());
        form.setPrice(product.getPrice());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        return form;
    }

    private void applyFormToProduct(ProductForm form, Product product, Category category) {
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setSku(form.getSku());
        product.setPrice(form.getPrice());
        product.setCategory(category);
    }
}
