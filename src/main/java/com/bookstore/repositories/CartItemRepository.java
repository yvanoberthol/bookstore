package com.bookstore.repositories;

import com.bookstore.entities.content.CartItem;
import com.bookstore.entities.content.Order;
import com.bookstore.entities.content.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findByShoppingCart(ShoppingCart shoppingCart);
    List<CartItem> findByOrder(Order order);
}
