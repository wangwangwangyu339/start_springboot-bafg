package com.example.demo.service.impl;

import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.result.PageResult;
import com.example.demo.entity.pg.CreateUserRequest;
import com.example.demo.entity.pg.UpdateUserRequest;
import com.example.demo.entity.pg.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务实现
 * <p>
 * - @Cacheable：查询自动缓存到 Upstash Redis
 * - @CacheEvict：更新/删除时自动清除对应缓存
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 根据 ID 查询（结果缓存至 Redis，key = "users::user::{id}"）
     */
    @Override
    @Cacheable(cacheNames = CacheConstants.USER_CACHE, key = "'user::' + #id")
    public User getById(Long id) {
        log.debug("[UserService] getById from DB, id={}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在，id=" + id);
        }
        return user;
    }

    /**
     * 分页查询（不缓存，实时查询）
     */
    @Override
    public PageResult<User> list(int page, int size) {
        int offset = (page - 1) * size;
        List<User> users = userMapper.selectList(offset, size);
        long total = userMapper.count();
        return PageResult.of(users, page, size, total);
    }

    /**
     * 新增用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(CreateUserRequest request) {
        // 检查用户名唯一
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在：" + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .avatarUrl(request.getAvatarUrl())
                .status(1)
                .build();
        userMapper.insert(user);
        log.info("[UserService] 用户创建成功, id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    /**
     * 更新用户，同时清除 Redis 缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConstants.USER_CACHE, key = "'user::' + #id")
    public User update(Long id, UpdateUserRequest request) {
        // 确认存在
        getById(id);
        User user = User.builder()
                .id(id)
                .email(request.getEmail())
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .avatarUrl(request.getAvatarUrl())
                .status(request.getStatus())
                .build();
        userMapper.updateById(user);
        log.info("[UserService] 用户更新成功, id={}", id);
        // 返回最新数据（已清除缓存，下次查询重新入缓存）
        return userMapper.selectById(id);
    }

    /**
     * 删除用户，同时清除 Redis 缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConstants.USER_CACHE, key = "'user::' + #id"),
            @CacheEvict(cacheNames = CacheConstants.USER_DETAIL, key = "#id")
    })
    public void delete(Long id) {
        int rows = userMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "用户不存在，id=" + id);
        }
        log.info("[UserService] 用户删除成功, id={}", id);
    }
}
