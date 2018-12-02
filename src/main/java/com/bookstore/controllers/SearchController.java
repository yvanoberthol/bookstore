package com.bookstore.controllers;

import com.bookstore.entities.content.Book;
import com.bookstore.entities.security.User;
import com.bookstore.services.content.BookService;
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
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @RequestMapping("/searchByCategory")
    public String searchCategory(
            @RequestParam("category") String category,
            Model model, Principal principal
    ){
        if (principal != null){
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("user",user);
        }

        String classActiveCategory = "active"+category;
        classActiveCategory = classActiveCategory.replaceAll("\\s+","");
        classActiveCategory = classActiveCategory.replaceAll("&","");
        model.addAttribute(classActiveCategory,true);

        List<Book> bookList = bookService.findByCategory(category);

        if (bookList.isEmpty()){
            model.addAttribute("emptyList",true);
            return "bookshelf";
        }


        model.addAttribute("bookList",bookList);

        return "bookshelf";

    }

    @RequestMapping(value = "/searchBook", method = RequestMethod.POST)
    public String searchBook(@ModelAttribute("keyword") String keyword,
                             Principal principal, Model model){

        if (principal != null){
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("user",user);
        }

        List<Book> bookList = bookService.fuzzySearch(keyword);

        if (bookList.isEmpty()){
            model.addAttribute("emptyList",true);
            return "bookshelf";
        }

        model.addAttribute("bookList",bookList);

        return "bookshelf";
    }
}
