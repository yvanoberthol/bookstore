package com.bookstore.repositories;

import com.bookstore.entities.content.UserBilling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBillingRepository extends JpaRepository<UserBilling,Long> {
}
