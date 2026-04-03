package com.example.demo.service;

import com.example.demo.common.result.PageResult;
import com.example.demo.entity.pg.CreateUserRequest;
import com.example.demo.entity.pg.UpdateUserRequest;
import com.example.demo.entity.pg.User;

/**
 * 用户业务接口
 */
public interface UserService {

    User getById(Long id);

    PageResult<User> list(int page, int size);

    User create(CreateUserRequest request);

    User update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
