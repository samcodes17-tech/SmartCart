package com.smartcart.service;

import com.smartcart.entity.Cart;
import com.smartcart.entity.CartItem;
import com.smartcart.entity.Product;
import com.smartcart.entity.User;
import com.smartcart.repository.CartItemRepository;
import com.smartcart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }


    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    public List<CartItem> getItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCartId(cart.getId());
    }

    public void addToCart(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.findById(productId);


        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
    }

    public void updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem item = getOwnedItem(user, cartItemId);
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    public void removeItem(User user, Long cartItemId) {
        CartItem item = getOwnedItem(user, cartItemId);
        cartItemRepository.delete(item);
    }

    public BigDecimal calculateTotal(User user) {
        return getItems(user).stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public int getTotalItemCount(User user){
        return getItems(user).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void clearCart(Cart cart) {
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartItem getOwnedItem(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("This cart item does not belong to the current user");
        }
        return item;
    }
}
