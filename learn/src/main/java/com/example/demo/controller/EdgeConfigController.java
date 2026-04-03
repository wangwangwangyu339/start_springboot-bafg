package com.example.demo.controller;

import com.example.demo.common.result.R;
import com.example.demo.config.EdgeConfigManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Vercel Edge Config 查询接口
 */
@Tag(name = "Edge Config", description = "Vercel 全局边缘配置管理")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class EdgeConfigController {

    private final EdgeConfigManager edgeConfigManager;

    @Operation(summary = "获取全量配置快照")
    @GetMapping
    public R<Map<String, Object>> getAll() {
        return R.ok(edgeConfigManager.getAll());
    }

    @Operation(summary = "获取 String 类型配置")
    @GetMapping("/string/{key}")
    public R<String> getString(
            @Parameter(description = "配置键") @PathVariable String key,
            @Parameter(description = "默认值") @RequestParam(defaultValue = "") String defaultValue) {
        return R.ok(edgeConfigManager.getString(key, defaultValue));
    }

    @Operation(summary = "获取 Boolean 类型配置（功能开关）")
    @GetMapping("/bool/{key}")
    public R<Boolean> getBoolean(
            @PathVariable String key,
            @RequestParam(defaultValue = "false") boolean defaultValue) {
        return R.ok(edgeConfigManager.getBoolean(key, defaultValue));
    }

    @Operation(summary = "获取 Int 类型配置（限流阈值等）")
    @GetMapping("/int/{key}")
    public R<Integer> getInt(
            @PathVariable String key,
            @RequestParam(defaultValue = "0") int defaultValue) {
        return R.ok(edgeConfigManager.getInt(key, defaultValue));
    }

    @Operation(summary = "手动触发配置刷新")
    @PostMapping("/refresh")
    public R<String> refresh() {
        edgeConfigManager.refresh();
        return R.ok("配置刷新成功");
    }
}
