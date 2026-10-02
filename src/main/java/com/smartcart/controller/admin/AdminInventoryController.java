package com.smartcart.controller.admin;

import com.smartcart.dto.InventorySettingsForm;
import com.smartcart.service.InventoryService;
import com.smartcart.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final SupplierService supplierService;

    public AdminInventoryController(InventoryService inventoryService, SupplierService supplierService) {
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {

        inventoryService.syncMissingInventory();
        model.addAttribute("inventoryList", inventoryService.findAll());
        return "admin/inventory/list";
    }

    @PostMapping("/{id}/stock-in")
    public String stockIn(@PathVariable Long id, @RequestParam int quantity) {
        try {
            inventoryService.stockIn(id, quantity);
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/inventory?error=" + ex.getMessage().replace(" ", "+");
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/{id}/stock-out")
    public String stockOut(@PathVariable Long id, @RequestParam int quantity) {
        try {
            inventoryService.stockOut(id, quantity);
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/inventory?error=" + ex.getMessage().replace(" ", "+");
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var inventory = inventoryService.findById(id);
        InventorySettingsForm form = new InventorySettingsForm();
        form.setReorderLevel(inventory.getReorderLevel());
        form.setSupplierId(inventory.getSupplier() != null ? inventory.getSupplier().getId() : null);

        model.addAttribute("inventory", inventory);
        model.addAttribute("settingsForm", form);
        model.addAttribute("suppliers", supplierService.findAll());
        return "admin/inventory/edit";
    }

    @PostMapping("/{id}")
    public String updateSettings(@PathVariable Long id,
                                  @Valid @ModelAttribute("settingsForm") InventorySettingsForm form,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("inventory", inventoryService.findById(id));
            model.addAttribute("suppliers", supplierService.findAll());
            return "admin/inventory/edit";
        }
        inventoryService.updateSettings(id, form);
        return "redirect:/admin/inventory";
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        model.addAttribute("inventory", inventoryService.findById(id));
        model.addAttribute("transactions", inventoryService.findHistory(id));
        return "admin/inventory/history";
    }
}
