package com.bookstore.repositories;

import com.bookstore.entities.content.BookToCardItem;
import com.bookstore.entities.content.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookToCartItemRepository extends JpaRepository<BookToCardItem,Long> {

    void deleteByCartItem(CartItem cartItem);
}
