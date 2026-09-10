package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图表
 */
@Data
public class Banner {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    /** 0-禁用，1-启用 */
    private Integer status;
    private LocalDateTime createTime;
}
