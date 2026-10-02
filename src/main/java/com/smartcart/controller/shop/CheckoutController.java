package com.smartcart.controller.shop;

import com.smartcart.entity.Order;
import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import com.smartcart.service.CartService;
import com.smartcart.service.OrderService;
import com.smartcart.service.RazorpayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final RazorpayService razorpayService;
    private final UserRepository userRepository;

    public CheckoutController(CartService cartService, OrderService orderService,
                              RazorpayService razorpayService, UserRepository userRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.razorpayService = razorpayService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String checkout(Principal principal, Model model) {
        User user = currentUser(principal);
        BigDecimal total = cartService.calculateTotal(user);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/cart?error=Your+cart+is+empty";
        }

        String razorpayOrderId = razorpayService.createOrder(total);

        model.addAttribute("total", total);
        model.addAttribute("amountInPaise", total.multiply(BigDecimal.valueOf(100)).longValue());
        model.addAttribute("razorpayOrderId", razorpayOrderId);
        model.addAttribute("razorpayKeyId", razorpayService.getKeyId());
        model.addAttribute("cartItems", cartService.getItems(user));
        return "checkout";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("razorpay_order_id") String razorpayOrderId,
                         @RequestParam("razorpay_payment_id") String razorpayPaymentId,
                         @RequestParam("razorpay_signature") String razorpaySignature,
                         Principal principal) {

        boolean valid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        if (!valid) {
            return "redirect:/checkout?error=Payment+verification+failed.+Please+try+again";
        }

        User user = currentUser(principal);
        Order order;
        try {
            order = orderService.placeOrder(user);
        } catch (IllegalStateException ex) {
            return "redirect:/cart?error=" + ex.getMessage().replace(" ", "+");
        }

        orderService.recordPayment(order.getId(), razorpayOrderId, razorpayPaymentId);
        return "redirect:/orders?placed";
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}