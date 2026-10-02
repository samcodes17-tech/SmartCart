package com.smartcart.controller;

import com.smartcart.dto.RegisterForm;
import com.smartcart.entity.Order;
import com.smartcart.entity.OrderItem;
import com.smartcart.entity.Product;
import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import com.smartcart.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    public AuthController(UserService userService,
                          ProductService productService,
                          CategoryService categoryService,
                          UserRepository userRepository,
                          OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }


    @GetMapping("/")
    public String root() {
        return "landing";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";   // resolves to templates/login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        // Thymeleaf's th:object needs a bean to bind form fields to, even on first page load
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                            BindingResult bindingResult,
                            Model model) {


        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(form);
        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
    @GetMapping("/home")
    public String home(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));

        model.addAttribute("email", principal.getName());
        model.addAttribute("name", user.getName());

        if (user.getRole().name().equals("CUSTOMER")) {
            List<Product> allProducts = productService.findAll();
            model.addAttribute("recommendedProducts",
                    allProducts.size() > 4 ? allProducts.subList(0, 4) : allProducts);

            model.addAttribute("categories", categoryService.findAll());

            List<Order> recentOrders = orderService.findOrdersForUser(user);
            List<Order> limited = recentOrders.size() > 3 ? recentOrders.subList(0, 3) : recentOrders;
            List<RecentOrderView> views = new ArrayList<>();
            for (Order order : limited) {
                List<OrderItem> items = orderService.findItems(order.getId());
                String firstItem = items.isEmpty() ? ("Order #" + order.getId()) : items.get(0).getProduct().getName();
                views.add(new RecentOrderView(firstItem, order.getStatus().name()));
            }
            model.addAttribute("recentOrders", views);
        }

        return "home";
    }

    public record RecentOrderView(String productName, String status) {}

}
