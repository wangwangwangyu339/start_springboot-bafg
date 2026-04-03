package com.example.demo.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Vercel Edge Config 全局边缘配置管理
 * <p>
 * 通过 HTTP API 拉取 Vercel Edge Config，本地内存缓存，
 * 定时刷新（默认每60秒），支持功能开关、限流、全局配置。
 * </p>
 *
 * <h3>Edge Config REST API</h3>
 * <pre>
 * GET https://edge-config.vercel.com/{edgeConfigId}/items
 * Authorization: Bearer {token}
 * 或直接使用 connectionString 中携带的 token 参数
 * </pre>
 */
@Slf4j
@Configuration
public class EdgeConfigManager {

    @Value("${vercel.edge-config.connection-string:}")
    private String connectionString;

    /** 本地配置缓存：key -> rawValue(String) */
    private final AtomicReference<Map<String, Object>> cache =
            new AtomicReference<>(new ConcurrentHashMap<>());

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 项目启动 + 每 refreshIntervalSeconds 秒刷新一次
     */
    @Scheduled(fixedDelayString = "${vercel.edge-config.refresh-interval-seconds:60}000",
               initialDelay = 0)
    public void refresh() {
        if (connectionString == null || connectionString.isBlank()
                || connectionString.contains("your_edge_config_id")) {
            log.debug("[EdgeConfig] connectionString 未配置，跳过拉取");
            return;
        }
        try {
            // connectionString 格式：https://edge-config.vercel.com/{id}?token={token}
            // 对应 REST API items 端点
            String apiUrl = connectionString.replace(
                    "edge-config.vercel.com/",
                    "edge-config.vercel.com/") + "/items";

            String json = restTemplate.getForObject(apiUrl, String.class);
            if (json == null) return;

            JsonNode root = objectMapper.readTree(json);
            Map<String, Object> newCache = new ConcurrentHashMap<>();
            if (root.isArray()) {
                root.forEach(item -> {
                    String key = item.path("key").asText();
                    JsonNode valueNode = item.path("value");
                    newCache.put(key, valueNode.isTextual() ? valueNode.asText() : valueNode.toString());
                });
            }
            cache.set(newCache);
            log.info("[EdgeConfig] 配置刷新成功，共 {} 项", newCache.size());
        } catch (Exception e) {
            log.warn("[EdgeConfig] 配置刷新失败: {}", e.getMessage());
        }
    }

    // ─── 获取配置工具方法 ───────────────────────────────────────

    /**
     * 获取 String 类型配置
     *
     * @param key          配置键
     * @param defaultValue 默认值
     */
    public String getString(String key, String defaultValue) {
        return Optional.ofNullable(cache.get().get(key))
                .map(Object::toString)
                .orElse(defaultValue);
    }

    /**
     * 获取 Boolean 类型配置（功能开关）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = cache.get().get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取 Integer 类型配置（限流阈值等）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     */
    public int getInt(String key, int defaultValue) {
        Object val = cache.get().get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            log.warn("[EdgeConfig] key={} 无法解析为 int，使用默认值 {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取全量配置快照（只读）
     */
    public Map<String, Object> getAll() {
        return Map.copyOf(cache.get());
    }
}
