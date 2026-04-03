package com.example.demo.mapper;

import com.example.demo.entity.pg.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper（MyBatis XML 方式）
 */
@Mapper
public interface UserMapper {

    /**
     * 根据 ID 查询
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询所有（支持分页：offset + limit）
     */
    List<User> selectList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询总数
     */
    long count();

    /**
     * 新增用户，返回影响行数
     */
    int insert(User user);

    /**
     * 更新用户（只更新非 null 字段）
     */
    int updateById(User user);

    /**
     * 根据 ID 删除
     */
    int deleteById(@Param("id") Long id);
}
