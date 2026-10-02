package com.smartcart.service;

import com.smartcart.entity.Inventory;
import com.smartcart.entity.Order;
import com.smartcart.entity.OrderItem;
import com.smartcart.entity.Role;
import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public EmailService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void sendOrderConfirmation(Order order, List<OrderItem> items) {
        StringBuilder body = new StringBuilder();
        body.append("Hi,\n\nThanks for your order! Here's a summary:\n\n");
        body.append("Order #").append(order.getId()).append("\n\n");
        for (OrderItem item : items) {
            body.append("- ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" @ Rs.").append(item.getPriceAtPurchase())
                    .append("\n");
        }
        body.append("\nTotal: Rs.").append(order.getTotalAmount());
        body.append("\n\nWe'll notify you when your order ships.\n\nSmartCart");

        send(order.getUser().getEmail(), "Order Confirmation - #" + order.getId(), body.toString());
    }

    public void sendLowStockAlert(Inventory inventory) {
        String body = "Heads up \u2014 " + inventory.getProduct().getName() + " is running low.\n\n" +
                "Current stock: " + inventory.getQuantityInStock() + "\n" +
                "Reorder level: " + inventory.getReorderLevel() + "\n\n" +
                "Consider restocking soon.\n\nSmartCart Admin Alerts";

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            send(admin.getEmail(), "Low Stock Alert: " + inventory.getProduct().getName(), body);
        }
    }


    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
