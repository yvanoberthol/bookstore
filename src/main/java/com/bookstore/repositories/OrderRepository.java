package com.bookstore.repositories;

import com.bookstore.entities.content.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
