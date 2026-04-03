package com.example.webframework.infrastructure.mapper;

import com.example.webframework.domain.BookEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper {

    @Select("select id, title, author, price from book order by id desc")
    List<BookEntity> findAll();

    @Select("select id, title, author, price from book where id = #{id}")
    BookEntity findById(@Param("id") Long id);

    @Insert("insert into book (title, author, price) values (#{title}, #{author}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(BookEntity book);

        @Update("update book " +
            "set title = coalesce(#{title}, title), " +
            "author = coalesce(#{author}, author), " +
            "price = coalesce(#{price}, price) " +
            "where id = #{id}")
    int updateSelective(BookEntity book);

    @Delete("delete from book where id = #{id}")
    int deleteById(@Param("id") Long id);
}
