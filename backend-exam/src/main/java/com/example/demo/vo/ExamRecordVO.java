package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRecordVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String username;
    private Long examId;
    private String examName;
    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
}
