package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试记录表
 */
@Data
public class ExamRecord {

    private Long id;
    private Long userId;
    private Long examId;
    /** 考生答案 JSON 对象，如 {"1":"A","2":"BD"} */
    private String answers;
    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;
    /** 0-进行中，1-已交卷（已自动判分） */
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
}
