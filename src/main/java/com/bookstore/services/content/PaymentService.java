package com.bookstore.services.content;


import com.bookstore.entities.content.Payment;
import com.bookstore.entities.content.UserPayment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {
    public Payment setByUserPayment(UserPayment userPayment, Payment payment) {
        payment.setCardName(userPayment.getCardName());
        payment.setCardNumber(userPayment.getCardNumber());
        payment.setHolderName(userPayment.getHolderName());
        payment.setCvc(userPayment.getCvc());
        payment.setExpiryMonth(userPayment.getExpiryMonth());
        payment.setExpiryYear(userPayment.getExpiryYear());
        payment.setType(userPayment.getType());

        return payment;
    }
}
