package com.bookstore.controllers;

import com.bookstore.entities.content.Book;
import com.bookstore.entities.content.CartItem;
import com.bookstore.entities.content.ShoppingCart;
import com.bookstore.entities.security.User;
import com.bookstore.services.content.BookService;
import com.bookstore.services.content.CartItemService;
import com.bookstore.services.content.ShoppingCartService;
import com.bookstore.services.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/shoppingCart")
public class ShoppingCartContoller {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @RequestMapping("/cart")
    public String shoppingCart(Model model, Principal principal){
        User user = userService.findByUsername(principal.getName());
        ShoppingCart shoppingCart = user.getShoppingCart();

        List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);

        shoppingCartService.updateShoppingCart(shoppingCart);

        model.addAttribute("cartItemList",cartItemList);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("user", user);

        return "shoppingCart";
    }

    @RequestMapping(value = "/addItem", method = RequestMethod.POST)
    public String addItem(@ModelAttribute("id") Long id,
                          @ModelAttribute("qty") String qty,
                          Model model, Principal principal){

        User user = userService.findByUsername(principal.getName());
        Book book = bookService.getBook(id);

        if (Integer.parseInt(qty) > book.getStockNumber()){
            model.addAttribute("notEnoughStock",true);
            return "forward:/bookDetail?id="+book.getId();
        }

        CartItem cartItem = cartItemService.addBookToCartItem(book,user,Integer.parseInt(qty));
        model.addAttribute("addBookSuccess",true);

        return "forward:/bookDetail?id="+book.getId();
    }

    @RequestMapping(value = "/updateCartItem", method = RequestMethod.POST)
    public String updateCartItem(@ModelAttribute("idCartItem") Long cartItemId,
                                 @ModelAttribute("qty") int qty){
        CartItem cartItem = cartItemService.findById(cartItemId);
        cartItem.setQty(qty);
        cartItemService.updateCartItem(cartItem);

        return "forward:/shoppingCart/cart";

    }

    @RequestMapping(value = "/removeItem")
    public String removeItem(@RequestParam("id") Long id){
        cartItemService.removeCartItem(cartItemService.findById(id));
        return "forward:/shoppingCart/cart";
    }

}
