package com.example.demo.dto;

import lombok.Data;

import java.util.List;

/**
 * 手动创建试卷入参
 */
@Data
public class ExamCreateDTO {

    private String name;
    private Integer totalScore;
    private Integer duration;
    private List<Long> questionIds;
}
