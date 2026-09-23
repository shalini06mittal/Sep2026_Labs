package com.training.SpringBootDemo.mapper;

import com.training.SpringBootDemo.dto.BookSearchCriteria;
import com.training.SpringBootDemo.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookMapper {

//    @Results(id = "bookResultMap", value = {
//            @Result(column = "book_id",     property = "bookid", id = true),
//            @Result(column = "title",       property = "title"),
//            @Result(column = "author",      property = "author"),
//            @Result(column = "description", property = "desc"),
//            @Result(column = "price",       property = "price")
//    })
    @ResultMap("bookResultMap")
    @Select("SELECT book_id, title, author, description, price FROM book")
    List<Book> findAll();

    @Select("SELECT COUNT(*) FROM book")
    long count();

    @ResultMap("bookResultMap")
    @Select("SELECT book_id, title, author, description, price FROM book WHERE book_id = #{id}")
    Book findById(@Param("id") int id);

    @ResultMap("bookResultMap")
    @Select("SELECT book_id, title, author, description, price FROM book " +
            "WHERE LOWER(author) = LOWER(#{author})")
    List<Book> findAllByAuthor(@Param("author") String author);

    @Insert("INSERT INTO book (title, author, description, price) " +
            "VALUES (#{title}, #{author}, #{desc}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "bookid", keyColumn = "book_id")
    void insert(Book book);

    @Update("UPDATE book SET title = #{title}, author = #{author}, " +
            "description = #{desc}, price = #{price} WHERE book_id = #{bookid}")
    int update(Book book);

    @Delete("DELETE FROM book WHERE book_id = #{id}")
    int deleteById(@Param("id") int id);

    List<Book> search(BookSearchCriteria criteria);
}
