package com.example.demo.controller;

import com.example.demo.common.result.R;
import com.example.demo.entity.mongo.UserLog;
import com.example.demo.service.UserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 用户操作日志接口（MongoDB）
 */
@Tag(name = "用户日志", description = "用户操作日志管理（MongoDB）")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class UserLogController {

    private final UserLogService userLogService;

    @Operation(summary = "新增操作日志")
    @PostMapping
    public R<UserLog> addLog(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "操作类型: CREATE/UPDATE/DELETE/QUERY") @RequestParam String action,
            @Parameter(description = "模块") @RequestParam(defaultValue = "user") String module,
            @Parameter(description = "详情") @RequestParam(required = false) String detail) {
        UserLog log = userLogService.log(userId, action, module, "/api/logs", detail, "127.0.0.1");
        return R.ok("日志记录成功", log);
    }

    @Operation(summary = "查询用户操作日志（分页）")
    @GetMapping("/user/{userId}")
    public R<Page<UserLog>> listByUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(userLogService.listByUser(userId, page, size));
    }
}
