package com.training.SpringBootDemo.restapi;

import com.training.SpringBootDemo.constants.AppConstants;
import com.training.SpringBootDemo.dto.BookSearchCriteria;
import com.training.SpringBootDemo.entity.Book;
import com.training.SpringBootDemo.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")

public class BookRestController {

    private BookService bookService;

    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

//    @GetMapping
//    public List<Book> getBooks(){
//        return bookService.getAllBooks();
//    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable  int id){
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstants.STATUS, AppConstants.SUCCESS);
            map.put("book",bookService.getBookById(id) );
            System.out.println("get boook by id");
            return ResponseEntity.ok(map);
        }
        catch (RuntimeException e){
            System.out.println("get boook by id ERROR");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<Book> getBooks(@RequestParam(required = false) String author){
        if(author==null)
            return bookService.getAllBooks();
        return bookService.getBooksByAuthor(author);
    }



    @PostMapping
    public ResponseEntity<Object> addBook(@Valid @RequestBody Book book){
        System.out.println("Book "+book);
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstants.STATUS, AppConstants.SUCCESS);
            Book savedBook = bookService.addNewBook(book);
            map.put("book", savedBook );
            URI location = URI.create("/books/" + savedBook.getBookid());
            return ResponseEntity.created(location).body(savedBook);
        }
        catch (RuntimeException e){
            map.put(AppConstants.STATUS, AppConstants.FAILURE);
            map.put("error",e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateBook(@RequestBody  Book book){
        System.out.println("Book "+book);
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstants.STATUS, AppConstants.SUCCESS);
            map.put("book",bookService.updateBook(book) );
            return ResponseEntity.ok(map);
        }
        catch (RuntimeException e){
            map.put(AppConstants.STATUS, AppConstants.FAILURE);
            map.put("error",e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }
    @DeleteMapping ("/{id}")
    public ResponseEntity<Object> deleteBook(@PathVariable int id){
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstants.STATUS, AppConstants.SUCCESS);
            bookService.deleteBook(id);
            map.put("message", "Book deleted successfully");
            return ResponseEntity.ok(map);
        }
         catch (RuntimeException e){
            map.put(AppConstants.STATUS, AppConstants.FAILURE);
            map.put("error",e.getMessage()); }
        return ResponseEntity.badRequest().body(map);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setAuthor(author);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setIds(ids);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir.toLowerCase());

        Map<String, Object> map = new HashMap<>();
        map.put(AppConstants.STATUS, AppConstants.SUCCESS);
        map.put("books", bookService.searchBooks(criteria));
        return ResponseEntity.ok(map);
    }

}
