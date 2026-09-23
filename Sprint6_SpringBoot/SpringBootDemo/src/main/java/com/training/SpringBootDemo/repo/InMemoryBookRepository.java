package com.training.SpringBootDemo.repo;

import com.training.SpringBootDemo.entity.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryBookRepository implements BookRepository{
    private List<Book> bookList;

    public InMemoryBookRepository() {
        bookList = new ArrayList<>();
        bookList.add(
                new Book(1, "Core Java", "Hotsmann","Learn java fundamentals", 130.0));
        bookList.add(
                new Book(2,"HTML", "Kelly","Learn html for UI", 230.0));
        bookList.add(
                new Book( 3, "python", "ryan","Learn python fundamentals", 130.0));
        bookList.add(
                new Book( 4, "css", "kelly","Learn css for designing webpage", 130.0));
    }

    public long count(){
        return bookList.size();
    }
    @Override
    public List<Book> findAll(){
        return bookList;
    }

    @Override
    public Book save(Book book){
        for (Book ob : bookList){
            if(ob.getBookid() == book.getBookid())
                throw new RuntimeException("Book with id "+book.getBookid()+" already exists");
        }
        Book lastBook = bookList.get(bookList.size()-1);
        int id =  lastBook.getBookid()+ 1;
        book.setBookid(id);
        bookList.add(book);
        return book;

    }
    @Override
    public Book update(Book book){
        for (int i=0;i<bookList.size();i++){
            if(bookList.get(i).getBookid() == book.getBookid()) {
                bookList.set(i, book);
                return book;
            }
        }
        throw new RuntimeException("Book with id "+book.getBookid()+" does not exist");
    }
    @Override
    public void delete(int id){
        for (int i=0;i<bookList.size();i++){
            if(bookList.get(i).getBookid() == id) {
                bookList.remove(i);
                return;
            }
        }
        throw new RuntimeException("Book with id "+id+" does not exist");
    }
    @Override
    public List<Book> findAllByAuthor(String author){
        List<Book> booksByAuthor = new ArrayList<>();
        for (Book ob : bookList){
            if(ob.getAuthor().equalsIgnoreCase (author))
                booksByAuthor.add(ob);
        }
        return booksByAuthor;
    }
    @Override
    public Book findBookById(int id){

        for (Book ob : bookList) {
            if (ob.getBookid() == id)
                return ob;
        }
        throw new RuntimeException("Book with id "+id+" does not exists");
    }
}
