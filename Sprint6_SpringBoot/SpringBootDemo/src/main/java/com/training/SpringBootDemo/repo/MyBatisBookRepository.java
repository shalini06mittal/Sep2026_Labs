package com.training.SpringBootDemo.repo;

import com.training.SpringBootDemo.dto.BookSearchCriteria;
import com.training.SpringBootDemo.entity.Book;
import com.training.SpringBootDemo.mapper.BookMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class MyBatisBookRepository implements BookRepository {

    private final BookMapper bookMapper;

    public MyBatisBookRepository(BookMapper bookMapper) {
        System.out.println("My Book Repo with mapper");
        this.bookMapper = bookMapper;
    }

    @Override
    public long count() {
        return bookMapper.count();
    }

    @Override
    public List<Book> findAll() {
        System.out.println("find all books");
        return bookMapper.findAll();
    }

    @Override
    public Book save(Book book) {
        if (book.getBookid() > 0 && bookMapper.findById(book.getBookid()) != null) {
            throw new RuntimeException("Book with id " + book.getBookid() + " already exists");
        }
        bookMapper.insert(book);      // the generated id is set on 'book'
        return book;
    }

    @Override
    public Book update(Book book) {
        if (bookMapper.update(book) == 0) {
            throw new RuntimeException("Book with id " + book.getBookid() + " does not exist");
        }
        return book;
    }

    @Override
    public List<Book> findAllByAuthor(String author) {
        return bookMapper.findAllByAuthor(author);
    }

    @Override
    public void delete(int id) {
        if (bookMapper.deleteById(id) == 0) {
            throw new RuntimeException("Book with id " + id + " does not exist");
        }
    }

    @Override
    public Book findBookById(int id) {
        Book book = bookMapper.findById(id);
        if (book == null) {
            throw new RuntimeException("Book with id " + id + " does not exists");
        }
        return book;
    }

    @Override
    public List<Book> search(BookSearchCriteria criteria) {
        return bookMapper.search(criteria);
    }
}