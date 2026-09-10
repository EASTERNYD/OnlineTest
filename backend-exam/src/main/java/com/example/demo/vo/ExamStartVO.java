package com.example.demo.vo;

import lombok.Data;

import java.util.List;

/**
 * 开始考试返回：记录ID + 试卷信息 + 题目（不含答案）
 */
@Data
public class ExamStartVO {

    private Long recordId;
    private Long examId;
    private String name;
    private Integer duration;
    private Integer totalScore;
    private List<QuestionVO> questions;
}
