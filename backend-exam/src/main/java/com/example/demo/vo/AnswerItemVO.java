package com.example.demo.vo;

import lombok.Data;

import java.util.List;

@Data
public class AnswerItemVO {

    private Long questionId;
    private String stem;
    private List<String> options;
    private Integer type;
    private String correctAnswer;
    private String userAnswer;
    private Boolean correct;
    private String analysis;
}
