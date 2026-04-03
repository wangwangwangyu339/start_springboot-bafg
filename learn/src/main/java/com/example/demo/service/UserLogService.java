package com.example.demo.service;

import com.example.demo.entity.mongo.UserLog;
import org.springframework.data.domain.Page;

/**
 * 用户日志业务接口（MongoDB）
 */
public interface UserLogService {

    /**
     * 记录操作日志
     */
    UserLog log(Long userId, String action, String module, String requestPath, String detail, String ip);

    /**
     * 分页查询用户日志
     */
    Page<UserLog> listByUser(Long userId, int page, int size);
}
