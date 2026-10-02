package com.smartcart.controller.admin;

import com.smartcart.entity.OrderStatus;
import com.smartcart.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("items", orderService.findItems(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/orders/" + id;
    }
}
