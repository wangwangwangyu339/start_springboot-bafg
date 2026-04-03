package com.example.demo.service.impl;

import com.example.demo.entity.mongo.UserLog;
import com.example.demo.mapper.UserLogRepository;
import com.example.demo.service.UserLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * 用户日志业务实现（MongoDB）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogServiceImpl implements UserLogService {

    private final UserLogRepository userLogRepository;

    @Override
    public UserLog log(Long userId, String action, String module,
                       String requestPath, String detail, String ip) {
        UserLog entry = UserLog.builder()
                .userId(userId)
                .action(action)
                .module(module)
                .requestPath(requestPath)
                .detail(detail)
                .ip(ip)
                .build();
        UserLog saved = userLogRepository.save(entry);
        log.debug("[UserLogService] 日志记录成功 id={}", saved.getId());
        return saved;
    }

    @Override
    public Page<UserLog> listByUser(Long userId, int page, int size) {
        return userLogRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page - 1, size));
    }
}
