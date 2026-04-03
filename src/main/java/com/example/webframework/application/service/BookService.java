package com.example.webframework.application.service;

import com.example.webframework.domain.BookEntity;
import com.example.webframework.infrastructure.mapper.BookMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<BookEntity> listBooks() {
        return bookMapper.findAll();
    }

    public Optional<BookEntity> getBook(Long id) {
        return Optional.ofNullable(bookMapper.findById(id));
    }

    public BookEntity createBook(BookEntity request) {
        request.setId(null);
        bookMapper.insert(request);
        return request;
    }

    public Optional<BookEntity> updateBook(Long id, BookEntity request) {
        BookEntity exists = bookMapper.findById(id);
        if (exists == null) {
            return Optional.empty();
        }
        BookEntity toUpdate = new BookEntity();
        toUpdate.setId(id);
        toUpdate.setTitle(request.getTitle());
        toUpdate.setAuthor(request.getAuthor());
        toUpdate.setPrice(request.getPrice());
        bookMapper.updateSelective(toUpdate);
        return Optional.ofNullable(bookMapper.findById(id));
    }

    public boolean deleteBook(Long id) {
        return bookMapper.deleteById(id) > 0;
    }
}
