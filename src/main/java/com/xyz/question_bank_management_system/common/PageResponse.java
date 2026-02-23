package com.xyz.question_bank_management_system.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 列表分页返回结构，与接口文档保持一致：page/size/total/list。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private long page;
    private long size;
    private long total;
    private List<T> list;

    public static <T> PageResponse<T> of(long page, long size, long total, List<T> list) {
        return new PageResponse<>(page, size, total, list);
    }
}
