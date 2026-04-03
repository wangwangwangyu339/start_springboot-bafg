package com.example.demo.feign;

import com.example.demo.feign.dto.JsonPlaceholderPost;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 示例 Feign 客户端：调用 JSONPlaceholder 公开接口
 * <p>
 * 生产环境替换为内部微服务地址，并配置服务发现（Nacos/Eureka/K8s）。
 * </p>
 */
@FeignClient(name = "jsonplaceholder", url = "${feign.clients.jsonplaceholder.url:https://jsonplaceholder.typicode.com}")
public interface JsonPlaceholderClient {

    /**
     * 获取所有帖子
     */
    @GetMapping("/posts")
    List<JsonPlaceholderPost> getPosts();

    /**
     * 根据 ID 获取帖子
     */
    @GetMapping("/posts/{id}")
    JsonPlaceholderPost getPostById(@PathVariable("id") Long id);
}
