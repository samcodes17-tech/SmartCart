package com.smartcart.controller.shop;

import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import com.smartcart.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String view(Principal principal, Model model) {
        User user = currentUser(principal);
        model.addAttribute("items", cartService.getItems(user));
        model.addAttribute("total", cartService.calculateTotal(user));
        return "cart/view";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, Principal principal) {
        cartService.addToCart(currentUser(principal), productId, quantity);
        return "redirect:/products";
    }

    @PostMapping("/items/{id}/update")
    public String update(@PathVariable Long id, @RequestParam int quantity, Principal principal) {
        cartService.updateQuantity(currentUser(principal), id, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/items/{id}/remove")
    public String remove(@PathVariable Long id, Principal principal) {
        cartService.removeItem(currentUser(principal), id);
        return "redirect:/cart";
    }


    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}
