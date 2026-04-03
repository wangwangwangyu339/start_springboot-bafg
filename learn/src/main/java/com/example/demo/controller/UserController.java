package com.example.demo.controller;

import com.example.demo.common.result.PageResult;
import com.example.demo.common.result.R;
import com.example.demo.entity.pg.CreateUserRequest;
import com.example.demo.entity.pg.UpdateUserRequest;
import com.example.demo.entity.pg.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口（PostgreSQL + MyBatis + Redis Cache）
 */
@Tag(name = "用户管理", description = "用户 CRUD 接口，含 Upstash Redis 自动缓存")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表（分页）")
    @GetMapping
    public R<PageResult<User>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        return R.ok(userService.list(page, size));
    }

    @Operation(summary = "根据 ID 获取用户（自动缓存）")
    @GetMapping("/{id}")
    public R<User> getById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public R<User> create(@Valid @RequestBody CreateUserRequest request) {
        return R.ok("用户创建成功", userService.create(request));
    }

    @Operation(summary = "更新用户（自动清除缓存）")
    @PutMapping("/{id}")
    public R<User> update(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return R.ok("用户更新成功", userService.update(id, request));
    }

    @Operation(summary = "删除用户（自动清除缓存）")
    @DeleteMapping("/{id}")
    public R<Void> delete(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }
}
