package com.training.SpringBootDemo.repo;

import com.training.SpringBootDemo.dto.BookSearchCriteria;
import com.training.SpringBootDemo.entity.Book;

import java.util.List;

public interface BookRepository {
    public long count();
    public List<Book> findAll();
    public Book save(Book book);
    public Book update(Book book);
    public List<Book>  findAllByAuthor(String author);
    public void delete(int id);
    public Book findBookById(int id);
    default List<Book> search(BookSearchCriteria criteria) {
        throw new UnsupportedOperationException("Search is supported only by the database repository");
    }
}
