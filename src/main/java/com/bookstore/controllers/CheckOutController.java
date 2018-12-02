package com.bookstore.controllers;

import com.bookstore.entities.content.*;
import com.bookstore.entities.security.User;
import com.bookstore.services.content.*;
import com.bookstore.services.mail.MailConfig;
import com.bookstore.services.security.UserService;
import com.bookstore.utility.USConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
public class CheckOutController {

    private ShippingAddress shippingAddress = new ShippingAddress();
    private BillingAddress billingAddress = new BillingAddress();
    private Payment payment = new Payment();

    @Autowired
    private UserService userService;

    @Autowired
    private UserShippingService userShippingService;

    @Autowired
    private UserPaymentService userPaymentService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ShippingAddressService shippingAddressService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BillingAddressService billingAddressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    @Qualifier("sendMailer")
    private MailConfig mailConfig;

    @RequestMapping(value = "/checkout")
    public String checkout(@RequestParam("id") Long cartId,
                           @RequestParam(value = "missingRequiredField", required = false) boolean missingRequiredField,
                           Model model, Principal principal){
        User user = userService.findByUsername(principal.getName());

        if (!cartId.equals(user.getShoppingCart().getIdShoppingCart())){
            return "badRequestPage";
        }

        List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());

        if (cartItemList.size() == 0){
            model.addAttribute("emptyCart", true);
            return "forward:/shoppingCart/cart";
        }

        for (CartItem cartItem : cartItemList){
            if (cartItem.getBook().getStockNumber() < cartItem.getQty()){
                model.addAttribute("notEnoughStock", true);
                return "forward:/shoppingCart/cart";
            }
        }

        List<UserShipping> userShippingList = user.getUserShippingList();
        List<UserPayment> userPaymentList = user.getUserPaymentList();

        model.addAttribute("userShippingList",userShippingList);
        model.addAttribute("userPaymentList",userPaymentList);

        if (userShippingList.size() == 0){
            model.addAttribute("emptyShippingList", true);
        }else{
            model.addAttribute("emptyShippingList", false);
        }

        if (userPaymentList.size() == 0){
            model.addAttribute("emptyPaymentList", true);
        }else{
            model.addAttribute("emptyPaymentList", false);
        }

        ShoppingCart shoppingCart = user.getShoppingCart();

        for (UserShipping userShipping : userShippingList){
            if (userShipping.isUserShippingDefault()){
                shippingAddressService.setByUserShipping(userShipping, shippingAddress);
            }
        }

        for (UserPayment userPayment : userPaymentList){
            if (userPayment.isDefaultPayment()){
                paymentService.setByUserPayment(userPayment, payment);
                billingAddressService.setByUserBilling(userPayment.getUserBilling(),billingAddress);
            }
        }

        model.addAttribute("shippingAddress",shippingAddress);
        model.addAttribute("user",user);
        model.addAttribute("payment", payment);
        model.addAttribute("billingAddress", billingAddress);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("cartItemList", cartItemList);

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);

        List<String> countryList = USConstants.listOfCountryCode;
        Collections.sort(countryList);
        model.addAttribute("stateList",stateList);
        model.addAttribute("countryList",countryList);


        model.addAttribute("classActiveShipping",true);

        if (missingRequiredField){
            model.addAttribute("missingRequiredField",true);
        }

        return "checkout";
    }

    @RequestMapping(value = "/checkout",method = RequestMethod.POST)
    public String checkoutPost(@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
                               @ModelAttribute("billingAddress") BillingAddress billingAddress,
                               @ModelAttribute("payment") Payment payment,
                               @ModelAttribute("billingSameAsShipping") String billingSameAsShipping,
                               @ModelAttribute("shippingMethod") String shippingMethod,
                               Principal principal, Model model){

        ShoppingCart shoppingCart = userService.findByUsername(principal.getName()).getShoppingCart();

        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
        model.addAttribute("cartItemList", cartItemList);

        if (billingSameAsShipping.equals("true")){
            billingAddress.setBillingAddressName(shippingAddress.getShippingAddressName());
            billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
            billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
            billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
            billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
            billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
            billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());
        }

        if (shippingAddress.getShippingAddressStreet1().isEmpty() ||
                shippingAddress.getShippingAddressCity().isEmpty()||
                shippingAddress.getShippingAddressState().isEmpty()||
                shippingAddress.getShippingAddressName().isEmpty()||
                shippingAddress.getShippingAddressZipcode().isEmpty()||
                payment.getCardNumber().isEmpty() ||
                payment.getCvc() == 0 ||
                billingAddress.getBillingAddressStreet1().isEmpty()||
                billingAddress.getBillingAddressCity().isEmpty()||
                billingAddress.getBillingAddressState().isEmpty()||
                billingAddress.getBillingAddressName().isEmpty()||
                billingAddress.getBillingAddressZipcode().isEmpty()){
            return "redirect:/checkout?id="+shoppingCart.getIdShoppingCart()+"&missingRequiredField=true";
        }

        User user = userService.findByUsername(principal.getName());
        Order order = orderService.createOrder(shoppingCart, shippingAddress,
                billingAddress, payment, shippingMethod,user);

        mailConfig.constructOrderConfirmationEmail(user,order,Locale.FRANCE);

        shoppingCartService.clearShoppingCart(shoppingCart);

        LocalDate today = LocalDate.now();
        LocalDate estimatedDeliveryDate;

        if (shippingMethod.equals("groundShipping")){
            estimatedDeliveryDate= today.plusDays(5);
        }else{
            estimatedDeliveryDate= today.plusDays(3);
        }
        model.addAttribute("estimatedDeliveryDate",estimatedDeliveryDate);

        return "orderSubmittedPage";

    }

    @RequestMapping(value = "/setShippingAddress")
    public String setShippingAddress(@RequestParam("userShippingId") Long userShippingId,
                                     Model model, Principal principal){

        User user = userService.findByUsername(principal.getName());
        UserShipping userShipping = userShippingService.findById(userShippingId);

        if (!userShipping.getUser().getId().equals(user.getId())){
            return "badRequestPage";
        }else{
            shippingAddressService.setByUserShipping(userShipping,shippingAddress);

            List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());

            /*BillingAddress billingAddress = new BillingAddress();*/

            List<UserShipping> userShippingList = user.getUserShippingList();
            List<UserPayment> userPaymentList = user.getUserPaymentList();

            model.addAttribute("userShippingList",userShippingList);
            model.addAttribute("userPaymentList",userPaymentList);


            model.addAttribute("emptyShippingList", false);

            if (userPaymentList.size() == 0){
                model.addAttribute("emptyPaymentList", true);
            }else{
                model.addAttribute("emptyPaymentList", false);
            }

            model.addAttribute("shippingAddress",shippingAddress);
            model.addAttribute("user",user);
            model.addAttribute("payment", payment);
            model.addAttribute("billingAddress", billingAddress);
            model.addAttribute("shoppingCart", user.getShoppingCart());
            model.addAttribute("cartItemList", cartItemList);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);

            List<String> countryList = USConstants.listOfCountryCode;
            Collections.sort(countryList);
            model.addAttribute("stateList",stateList);
            model.addAttribute("countryList",countryList);

            model.addAttribute("shippingAddress",shippingAddress);

            model.addAttribute("classActiveShipping",true);
            return "checkout";

        }
    }
    @RequestMapping(value = "/setPaymentMethod")
    public String setPaymentMethod(@RequestParam("userPaymentId") Long userPaymentMethod,
                                     Model model, Principal principal){

        User user = userService.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentService.getUserPayment(userPaymentMethod);
        UserBilling userBilling = userPayment.getUserBilling();

        if (!userPayment.getUser().getId().equals(user.getId())){
            return "badRequestPage";
        }else{
            paymentService.setByUserPayment(userPayment,payment);

            List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
            billingAddressService.setByUserBilling(userBilling,billingAddress);


            List<UserShipping> userShippingList = user.getUserShippingList();
            List<UserPayment> userPaymentList = user.getUserPaymentList();

            model.addAttribute("userShippingList",userShippingList);
            model.addAttribute("userPaymentList",userPaymentList);

            model.addAttribute("emptyPaymentList", false);

            if (userPaymentList.size() == 0){
                model.addAttribute("emptyShippingList", true);
            }else{
                model.addAttribute("emptyShippingList", false);
            }

            model.addAttribute("shippingAddress",shippingAddress);
            model.addAttribute("user",user);
            model.addAttribute("payment", payment);
            model.addAttribute("billingAddress", billingAddress);
            model.addAttribute("shoppingCart", user.getShoppingCart());
            model.addAttribute("cartItemList", cartItemList);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);

            List<String> countryList = USConstants.listOfCountryCode;
            Collections.sort(countryList);
            model.addAttribute("stateList",stateList);
            model.addAttribute("countryList",countryList);

            model.addAttribute("shippingAddress",shippingAddress);

            model.addAttribute("classActivePayment",true);
            return "checkout";

        }
    }

}
