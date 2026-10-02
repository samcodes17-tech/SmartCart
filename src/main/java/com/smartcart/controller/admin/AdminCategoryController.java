package com.smartcart.controller.admin;

import com.smartcart.entity.Category;
import com.smartcart.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Every route here starts with /admin/, and SecurityConfig already restricts
// anything under /admin/** to users with ROLE_ADMIN — no extra security code needed here.
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "admin/categories/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("category") Category category,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/categories/list";
        }
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Model model) {
        try {
            categoryService.delete(id);
        } catch (IllegalStateException ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("category", new Category());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/categories/list";
        }
        return "redirect:/admin/categories";
    }
}
