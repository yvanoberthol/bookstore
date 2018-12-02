package com.bookstore.repositories;

import com.bookstore.entities.content.UserShipping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserShippingRepository extends JpaRepository<UserShipping,Long> {
}
