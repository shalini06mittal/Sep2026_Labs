package com.training.SpringBootDemo.service;

import com.training.SpringBootDemo.dto.BookSearchCriteria;
import com.training.SpringBootDemo.entity.Book;
import com.training.SpringBootDemo.repo.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {

    private BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
        System.out.println(this.repository);
    }
    public long getTotalBookCount(){
        return repository.count();
    }
    public List<Book> getAllBooks(){
        return repository.findAll();
    }
    public Book addNewBook(Book book) {
            return repository.save(book);
    }
    public Book updateBook(Book book) {
        Book savedBook = null;
        savedBook = repository.update(book);
        return savedBook;
    }
    public void deleteBook(int id) {
            repository.delete(id);
    }
    public List<Book> getBooksByAuthor(String author){
        return repository.findAllByAuthor(author);
    }
    public Book getBookById(int id)  {
            return repository.findBookById(id);

    }
    public List<Book> searchBooks(BookSearchCriteria criteria) {
        return repository.search(criteria);
    }
}


