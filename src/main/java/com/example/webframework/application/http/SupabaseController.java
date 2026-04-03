package com.example.webframework.application.http;

import com.example.webframework.application.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/supabase")
public class SupabaseController {

    @Autowired
    private SupabaseService supabaseService;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return supabaseService.healthCheck();
    }

    @GetMapping("/table/{table}")
    public List<Object> queryTable(
            @PathVariable("table") String table,
            @RequestParam(value = "select", defaultValue = "*") String select,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        try {
            return supabaseService.queryTable(table, select, limit);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/book")
    public List<Object> listBooks(
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return supabaseService.listBooks(limit);
    }

    @GetMapping("/book/{id}")
    public Map<String, Object> getBookById(@PathVariable("id") Long id) {
        try {
            return supabaseService.getBookById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/book")
    public Map<String, Object> createBook(@RequestBody Map<String, Object> payload) {
        try {
            return supabaseService.createBook(payload);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping("/book/{id}")
    public Map<String, Object> updateBook(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        try {
            return supabaseService.updateBook(id, payload);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/book/{id}")
    public Map<String, Object> deleteBook(@PathVariable("id") Long id) {
        try {
            return supabaseService.deleteBook(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
