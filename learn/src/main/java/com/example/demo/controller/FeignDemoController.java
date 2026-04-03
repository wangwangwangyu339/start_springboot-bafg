package com.example.demo.controller;

import com.example.demo.common.result.R;
import com.example.demo.feign.JsonPlaceholderClient;
import com.example.demo.feign.dto.JsonPlaceholderPost;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OpenFeign 调用示例接口
 */
@Tag(name = "Feign 调用示例", description = "演示 Spring Cloud OpenFeign 远程调用")
@RestController
@RequestMapping("/api/feign")
@RequiredArgsConstructor
public class FeignDemoController {

    private final JsonPlaceholderClient jsonPlaceholderClient;

    @Operation(summary = "调用外部服务：获取所有帖子")
    @GetMapping("/posts")
    public R<List<JsonPlaceholderPost>> getPosts() {
        return R.ok(jsonPlaceholderClient.getPosts());
    }

    @Operation(summary = "调用外部服务：根据ID获取帖子")
    @GetMapping("/posts/{id}")
    public R<JsonPlaceholderPost> getPostById(
            @Parameter(description = "帖子ID") @PathVariable Long id) {
        return R.ok(jsonPlaceholderClient.getPostById(id));
    }
}
