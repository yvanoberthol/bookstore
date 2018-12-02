package com.bookstore.services.content;


import com.bookstore.entities.content.*;
import com.bookstore.entities.security.User;
import com.bookstore.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemService cartItemService;

    public synchronized Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress, BillingAddress billingAddress,
                             Payment payment, String shippingMethod, User user) {
        Order order = new Order();
        order.setShippingAddress(shippingAddress);
        order.setPayment(payment);
        order.setOrderStatus("created");
        order.setBillingAddress(billingAddress);
        order.setShippingMethod(shippingMethod);

        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);

        for (CartItem cartItem: cartItemList){
            Book book = cartItem.getBook();
            cartItem.setOrder(order);
            book.setStockNumber(book.getStockNumber() - cartItem.getQty());
        }

        order.setCartItemList(cartItemList);
        order.setOrderDate(Calendar.getInstance().getTime());
        order.setOrderTotal(shoppingCart.getGrandTotal());
        shippingAddress.setOrder(order);
        billingAddress.setOrder(order);
        payment.setOrder(order);
        order.setUser(user);
        order = orderRepository.save(order);

        return order;

    }

    public Order getOrder(Long id){
        return orderRepository.getOne(id);
    }
}
