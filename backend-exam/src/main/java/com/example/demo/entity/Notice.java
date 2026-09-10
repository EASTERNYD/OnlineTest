package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告表
 */
@Data
public class Notice {

    private Long id;
    private String title;
    private String content;
    /** 1-通知，2-警告，3-活动 */
    private Integer type;
    /** 0-草稿/禁用，1-发布/启用 */
    private Integer status;
    private LocalDateTime createTime;
}
