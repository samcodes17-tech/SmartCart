package com.smartcart.controller;

import com.smartcart.repository.UserRepository;
import com.smartcart.service.CartService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository userRepository;
    private final CartService cartService;

    public GlobalModelAdvice(UserRepository userRepository, CartService cartService) {
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(Principal principal) {
        if (principal == null) {
            return 0;
        }
        return userRepository.findByEmail(principal.getName())
                .map(cartService::getTotalItemCount)
                .orElse(0);
    }
}