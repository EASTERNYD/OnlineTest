package com.example.demo.dto;

import lombok.Data;

@Data
public class QuestionQueryDTO {

    private Integer page = 1;
    private Integer size = 10;
    private Long categoryId;
    private Integer difficulty;
    private Integer type;
    private String keyword;
}
