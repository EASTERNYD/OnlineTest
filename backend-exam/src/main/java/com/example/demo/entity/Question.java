package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库表
 */
@Data
public class Question {

    private Long id;
    private Long categoryId;
    private String stem;
    /** 选项 JSON 数组，如 ["A. 北京", "B. 上海"] */
    private String options;
    /** 正确答案，单选 "A"，多选 "ABD"（排序后拼接） */
    private String answer;
    private String analysis;
    /** 1-简单，2-中等，3-困难 */
    private Integer difficulty;
    private Integer viewCount;
    /** 0-单选，1-多选 */
    private Integer type;
    private LocalDateTime createTime;
}
