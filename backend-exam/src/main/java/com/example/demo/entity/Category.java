package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表（支持树形结构）
 */
@Data
public class Category {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
