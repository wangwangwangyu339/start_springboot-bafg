package com.example.webframework.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harium.postgrest.Condition;
import com.harium.supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SupabaseService {

    private final ObjectMapper objectMapper;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String anonKey;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    public SupabaseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> healthCheck() {
        SupabaseClient client = createClient();
        return Map.of(
                "sdk", "com.harium.supabase:core",
                "configured", true,
                "host", extractHost(normalizeBaseUrl()),
                "client", client.getClass().getSimpleName()
        );
    }

    public List<Object> queryTable(String table, String select, Integer limit) {
        if (!table.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid table name");
        }

        String[] fields = toSelectFields(select);
        SupabaseClient client = createClient();
        try {
            String raw = client.find(table, Condition.select(fields));
            if (!StringUtils.hasText(raw)) {
                return Collections.emptyList();
            }

            List<Object> allRows = objectMapper.readValue(
                    raw,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
            );

            if (limit == null || limit <= 0 || allRows.size() <= limit) {
                return allRows;
            }
            return new ArrayList<>(allRows.subList(0, limit));
        } catch (IOException e) {
            throw new IllegalStateException("Supabase SDK query failed: " + e.getMessage(), e);
        }
    }

    public List<Object> listBooks(Integer limit) {
        return queryTable("book", "*", limit);
    }

    public Map<String, Object> getBookById(Long id) {
        SupabaseClient client = createClient();
        try {
            String raw = client.find("book", Condition.eq("id", toIntId(id)));
            return toSingleObject(raw);
        } catch (IOException e) {
            throw new IllegalStateException("Supabase SDK query failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> createBook(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Book payload cannot be empty");
        }
        SupabaseClient client = createClient();
        try {
            String raw = client.insert("book", objectMapper.writeValueAsString(payload));
            return toSingleObject(raw);
        } catch (IOException e) {
            throw new IllegalStateException("Supabase SDK insert failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> updateBook(Long id, Map<String, Object> payload) {
        Map<String, Object> body = payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload);
        body.put("id", toIntId(id));

        SupabaseClient client = createClient();
        try {
            String raw = client.save("book", objectMapper.writeValueAsString(body));
            return toSingleObject(raw);
        } catch (IOException e) {
            throw new IllegalStateException("Supabase SDK update failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> deleteBook(Long id) {
        SupabaseClient client = createClient();
        try {
            String raw = client.delete("book", Condition.eq("id", toIntId(id)));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("deletedId", id);
            result.put("response", toJsonValue(raw));
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Supabase SDK delete failed: " + e.getMessage(), e);
        }
    }

    private SupabaseClient createClient() {
        String host = extractHost(normalizeBaseUrl());
        return new SupabaseClient(host, chooseApiKey());
    }

    private String normalizeBaseUrl() {
        if (!StringUtils.hasText(supabaseUrl)) {
            throw new IllegalStateException("Missing config: supabase.url");
        }
        String normalized = supabaseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String extractHost(String baseUrl) {
        URI uri = URI.create(baseUrl);
        if (!StringUtils.hasText(uri.getHost())) {
            throw new IllegalStateException("Invalid config: supabase.url must be a valid https URL");
        }
        return uri.getHost();
    }

    private String[] toSelectFields(String select) {
        if (!StringUtils.hasText(select) || "*".equals(select.trim())) {
            return new String[]{"*"};
        }
        String[] parts = select.split(",");
        List<String> fields = new ArrayList<>();
        for (String part : parts) {
            String field = part.trim();
            if (!field.matches("^[a-zA-Z0-9_*]+$")) {
                throw new IllegalArgumentException("Invalid select field: " + field);
            }
            fields.add(field);
        }
        if (fields.isEmpty()) {
            return new String[]{"*"};
        }
        return fields.toArray(new String[0]);
    }

    private String chooseApiKey() {
        String key = StringUtils.hasText(serviceRoleKey) ? serviceRoleKey : anonKey;
        if (!StringUtils.hasText(key)) {
            throw new IllegalStateException("Missing config: supabase.anon-key or supabase.service-role-key");
        }
        return key;
    }

    private int toIntId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be a positive number");
        }
        if (id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("id is too large");
        }
        return id.intValue();
    }

    private Map<String, Object> toSingleObject(String raw) throws IOException {
        Object value = toJsonValue(raw);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return Collections.emptyMap();
            }
            Object first = list.get(0);
            if (first instanceof Map) {
                return (Map<String, Object>) first;
            }
            return Map.of("value", first);
        }
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of("value", value);
    }

    private Object toJsonValue(String raw) throws IOException {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        JsonNode node = objectMapper.readTree(raw);
        if (node == null || node.isNull()) {
            return Collections.emptyList();
        }
        if (node.isArray()) {
            return objectMapper.convertValue(
                    node,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
            );
        }
        if (node.isObject()) {
            return objectMapper.convertValue(
                    node,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );
        }
        return objectMapper.convertValue(node, Object.class);
    }
}
