package com.bookstore.repositories;

import com.bookstore.entities.content.UserPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPaymentRepository extends JpaRepository<UserPayment,Long> {

}
