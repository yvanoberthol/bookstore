package com.bookstore.services.content;

import com.bookstore.entities.content.Book;
import com.bookstore.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Book addBook(Book book){
        return bookRepository.save(book);
    }

    public List<Book> findAll(){
        List<Book> bookList = bookRepository.findAll();

        List<Book> activeBookList = new ArrayList<>();

        for (Book book : bookList){
            if (book.isStatus()){
                activeBookList.add(book);
            }
        }
        return activeBookList;
    }

    public Book getBook(Long id){
        return bookRepository.getOne(id);
    }


    public List<Book> findByCategory(String category) {
        List<Book> bookList = bookRepository.findByCategory(category);
        List<Book> activeBookList = new ArrayList<>();

        for (Book book : bookList){
            if (book.isStatus()){
                activeBookList.add(book);
            }
        }
        return activeBookList;
    }

    public List<Book> fuzzySearch(String keyword) {
        List<Book> bookList = bookRepository.findByTitleContaining(keyword);
        return bookList;
    }
}
