package com.example.demo.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private List<T> list;
}
