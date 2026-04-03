package com.example.demo.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页响应体
 *
 * @param <T> 列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页（从 1 开始） */
    private int page;

    /** 每页大小 */
    private int size;

    /** 总条数 */
    private long total;

    /** 总页数 */
    private int totalPages;

    /** 数据列表 */
    private List<T> list;

    public static <T> PageResult<T> of(List<T> list, int page, int size, long total) {
        return PageResult.<T>builder()
                .list(list)
                .page(page)
                .size(size)
                .total(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }
}
