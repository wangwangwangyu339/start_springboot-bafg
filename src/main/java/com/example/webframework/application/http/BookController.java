package com.example.webframework.application.http;

import com.example.webframework.application.service.BookService;
import com.example.webframework.domain.BookEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookEntity> list() {
        return bookService.listBooks();
    }

    @GetMapping("/{id}")
    public BookEntity detail(@PathVariable("id") Long id) {
        return bookService.getBook(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @PostMapping
    public BookEntity create(@RequestBody BookEntity payload) {
        return bookService.createBook(payload);
    }

    @PutMapping("/{id}")
    public BookEntity update(@PathVariable("id") Long id, @RequestBody BookEntity payload) {
        return bookService.updateBook(id, payload)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable("id") Long id) {
        boolean deleted = bookService.deleteBook(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return Map.of("deleted", true, "id", id);
    }
}
