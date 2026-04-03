package com.example.demo.feign.dto;

import lombok.Data;

/**
 * JSONPlaceholder Post DTO
 */
@Data
public class JsonPlaceholderPost {
    private Long userId;
    private Long id;
    private String title;
    private String body;
}
