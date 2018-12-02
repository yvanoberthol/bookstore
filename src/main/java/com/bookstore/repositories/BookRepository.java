package com.bookstore.repositories;

import com.bookstore.entities.content.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findByCategory(String category);


    List<Book> findByTitleContaining(String keyword);
}
