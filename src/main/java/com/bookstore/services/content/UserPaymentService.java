package com.bookstore.services.content;


import com.bookstore.entities.content.UserBilling;
import com.bookstore.entities.content.UserPayment;
import com.bookstore.entities.content.UserShipping;
import com.bookstore.entities.security.User;
import com.bookstore.repositories.UserBillingRepository;
import com.bookstore.repositories.UserPaymentRepository;
import com.bookstore.repositories.UserRepository;
import com.bookstore.repositories.UserShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserPaymentService {

    @Autowired
    UserPaymentRepository userPaymentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserBillingRepository userBillingRepository;

    @Autowired
    UserShippingRepository userShippingRepository;

    public UserPayment getUserPayment(Long id){
        return userPaymentRepository.getOne(id);
    }

    public void removeUserPayment(Long id){
         userPaymentRepository.deleteById(id);
    }

    public void setUserDefaultPayment(Long userPaymentId, User user) {
        List<UserPayment> userPayments = userPaymentRepository.findAll();

        for (UserPayment userPayment : userPayments){
            if (userPayment.getIdPayment().equals(userPaymentId)){
                userPayment.setDefaultPayment(true);
                userPaymentRepository.save(userPayment);
            }else{
                userPayment.setDefaultPayment(false);
                userPaymentRepository.save(userPayment);
            }
        }
    }

    public void addUserBilling(UserBilling userBilling, UserPayment userPayment, User user) {
        userPayment.setUser(user);
        userPayment.setUserBilling(userBilling);
        userBilling.setUserPayment(userPayment);
        user.getUserPaymentList().add(userPayment);
        userRepository.save(user);
    }

    public void addUserShipping(UserShipping userShipping, User user) {
        userShipping.setUser(user);
        userShipping.setUserShippingDefault(true);
        user.getUserShippingList().add(userShipping);
        userRepository.save(user);

    }

    public UserShipping getUserShipping(Long shippingId) {
        return userShippingRepository.getOne(shippingId);
    }

    public void removeUserShipping(Long shippingId) {
        userShippingRepository.deleteById(shippingId);
    }

    public void setUserDefaultShipping(Long userShippingId, User user) {
        List<UserShipping> userShippings = userShippingRepository.findAll();

        for (UserShipping userShipping : userShippings){
            if (userShipping.getIdShipping().equals(userShippingId)){
                userShipping.setUserShippingDefault(true);
                userShippingRepository.save(userShipping);
            }else{
                userShipping.setUserShippingDefault(false);
                userShippingRepository.save(userShipping);
            }
        }
    }
}
