package com.smartcart.controller.admin;

import com.smartcart.entity.Supplier;
import com.smartcart.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/suppliers")
public class AdminSupplierController {

    private final SupplierService supplierService;

    public AdminSupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        if (!model.containsAttribute("supplier")) {
            model.addAttribute("supplier", new Supplier());
        }
        return "admin/suppliers/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("supplier") Supplier supplier,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("suppliers", supplierService.findAll());
            return "admin/suppliers/list";
        }
        supplierService.save(supplier);
        return "redirect:/admin/suppliers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Model model) {
        try {
            supplierService.delete(id);
        } catch (IllegalStateException ex) {
            model.addAttribute("suppliers", supplierService.findAll());
            model.addAttribute("supplier", new Supplier());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/suppliers/list";
        }
        return "redirect:/admin/suppliers";
    }
}
