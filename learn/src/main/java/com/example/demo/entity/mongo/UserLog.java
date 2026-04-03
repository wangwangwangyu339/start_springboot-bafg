package com.example.demo.entity.mongo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 用户操作日志（映射 MongoDB user_log 集合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_log")
public class UserLog {

    /** MongoDB 文档主键 */
    @Id
    private String id;

    /** 关联用户 ID（已创建索引） */
    @Indexed
    @Field("user_id")
    private Long userId;

    /** 操作类型：CREATE / UPDATE / DELETE / QUERY */
    @Field("action")
    private String action;

    /** 操作模块 */
    @Field("module")
    private String module;

    /** 请求路径 */
    @Field("request_path")
    private String requestPath;

    /** 操作详情 */
    @Field("detail")
    private String detail;

    /** 客户端 IP */
    @Field("ip")
    private String ip;

    /** 创建时间 */
    @CreatedDate
    @Field("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
