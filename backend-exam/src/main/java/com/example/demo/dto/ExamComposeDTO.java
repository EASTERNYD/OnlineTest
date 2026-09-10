package com.example.demo.dto;

import lombok.Data;

import java.util.List;

/**
 * 智能组卷入参：按分类/难度/数量规则抽题
 */
@Data
public class ExamComposeDTO {

    private String name;
    private Integer duration;
    private List<ComposeRule> rules;

    @Data
    public static class ComposeRule {
        private Long categoryId;
        private Integer count;
        /** 可选，为空则不限定难度 */
        private Integer difficulty;
    }
}
