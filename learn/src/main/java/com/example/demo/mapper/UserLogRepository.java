package com.example.demo.mapper;

import com.example.demo.entity.mongo.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户日志 MongoDB Repository
 */
@Repository
public interface UserLogRepository extends MongoRepository<UserLog, String> {

    /**
     * 按 userId 分页查询
     */
    Page<UserLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 按 userId 和 action 查询
     */
    Page<UserLog> findByUserIdAndActionOrderByCreatedAtDesc(Long userId, String action, Pageable pageable);
}
