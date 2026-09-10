package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDetailVO {

    private Long id;
    private String name;
    private Integer totalScore;
    private Integer duration;
    private Integer status;
    private LocalDateTime createTime;
    private List<QuestionVO> questions;
}
