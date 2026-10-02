package com.smartcart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;
    private String razorpayOrderId;
    private String razorpayPaymentId;

    private LocalDateTime orderDate;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PLACED;
    }
}
