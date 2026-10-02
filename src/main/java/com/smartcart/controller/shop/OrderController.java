package com.smartcart.controller.shop;

import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import com.smartcart.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping("/orders/place")
    public String place() {
        return "redirect:/checkout";
    }

    @GetMapping("/orders")
    public String myOrders(Principal principal, Model model) {
        model.addAttribute("orders", orderService.findOrdersForUser(currentUser(principal)));
        return "orders/history";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        var order = orderService.findById(id);

        if (!order.getUser().getEmail().equals(principal.getName())) {
            throw new SecurityException("This order does not belong to the current user");
        }
        model.addAttribute("order", order);
        model.addAttribute("items", orderService.findItems(id));
        return "orders/detail";
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}
