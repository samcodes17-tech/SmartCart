package com.smartcart.controller.admin;

import com.smartcart.dto.ProductForm;
import com.smartcart.entity.Product;
import com.smartcart.service.CategoryService;
import com.smartcart.service.FileStorageService;
import com.smartcart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public AdminProductController(ProductService productService, CategoryService categoryService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formAction", "/admin/products");
        model.addAttribute("pageTitle", "Add Product");
        return "admin/products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                          BindingResult bindingResult,
                          @RequestParam(value = "image", required = false) MultipartFile imageFile,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formAction", "/admin/products");
            model.addAttribute("pageTitle", "Add Product");
            return "admin/products/form";
        }
        try {
            String imageUrl = fileStorageService.store(imageFile);
            productService.createProduct(form, imageUrl);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formAction", "/admin/products");
            model.addAttribute("pageTitle", "Add Product");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/products/form";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("productForm", productService.toForm(product));
        model.addAttribute("currentImageUrl", product.getImageUrl());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formAction", "/admin/products/" + id);
        model.addAttribute("pageTitle", "Edit Product");
        return "admin/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("productForm") ProductForm form,
                          BindingResult bindingResult,
                          @RequestParam(value = "image", required = false) MultipartFile imageFile,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formAction", "/admin/products/" + id);
            model.addAttribute("pageTitle", "Edit Product");
            return "admin/products/form";
        }
        try {
            String imageUrl = fileStorageService.store(imageFile);
            productService.updateProduct(id, form, imageUrl);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formAction", "/admin/products/" + id);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/products/form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
        } catch (IllegalStateException ex) {
            return "redirect:/admin/products?error=" + ex.getMessage().replace(" ", "+");
        }
        return "redirect:/admin/products";
    }
}
