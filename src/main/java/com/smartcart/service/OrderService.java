package com.smartcart.service;

import com.smartcart.entity.*;
import com.smartcart.repository.CartItemRepository;
import com.smartcart.repository.InventoryRepository;
import com.smartcart.repository.OrderItemRepository;
import com.smartcart.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository, InventoryRepository inventoryRepository,
                        InventoryService inventoryService, CartService cartService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.cartService = cartService;
        this.emailService = emailService;
    }

    @Transactional
    public Order placeOrder(User user) {
        Cart cart = cartService.getOrCreateCart(user);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new IllegalStateException("Your cart is empty");
        }

        for (CartItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("No inventory record for " + item.getProduct().getName()));
            if (item.getQuantity() > inventory.getQuantityInStock()) {
                throw new IllegalStateException(
                        "Not enough stock for " + item.getProduct().getName() +
                                " (" + inventory.getQuantityInStock() + " available, " + item.getQuantity() + " requested)");
            }
        }

        Order order = new Order();
        order.setUser(user);
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> savedItems = new java.util.ArrayList<>();
        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(item.getProduct().getPrice());
            orderItemRepository.save(orderItem);
            savedItems.add(orderItem);

            total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId()).orElseThrow();
            inventoryService.stockOut(inventory.getId(), item.getQuantity(), "Order #" + order.getId() + " placed");
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        cartService.clearCart(cart);

        emailService.sendOrderConfirmation(order, savedItems);

        return order;
    }

    public List<Order> findOrdersForUser(User user) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    public List<OrderItem> findItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findById(orderId);
        boolean wasAlreadyCancelled = order.getStatus() == OrderStatus.CANCELLED;

        if (newStatus == OrderStatus.CANCELLED && !wasAlreadyCancelled) {
            for (OrderItem item : findItems(orderId)) {
                Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId()).orElseThrow();
                inventoryService.stockIn(inventory.getId(), item.getQuantity(), "Order #" + orderId + " cancelled — stock restored");
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public void recordPayment(Long orderId, String razorpayOrderId, String razorpayPaymentId) {
        Order order = findById(orderId);
        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);
        orderRepository.save(order);
    }
}