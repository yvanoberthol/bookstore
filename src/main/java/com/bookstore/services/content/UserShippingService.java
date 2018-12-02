package com.bookstore.services.content;

import com.bookstore.entities.content.UserShipping;
import com.bookstore.repositories.UserShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserShippingService {
    @Autowired
    UserShippingRepository userShippingRepository;

    public UserShipping findById(Long userShippingId) {
        return userShippingRepository.getOne(userShippingId);
    }
}
