package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 试卷表
 */
@Data
public class Exam {

    private Long id;
    private String name;
    private Integer totalScore;
    /** 考试时长（分钟） */
    private Integer duration;
    /** 题目 ID 列表 JSON 数组，如 [1,2,3] */
    private String questionIds;
    /** 0-草稿，1-已发布，2-已结束 */
    private Integer status;
    private LocalDateTime createTime;
}
