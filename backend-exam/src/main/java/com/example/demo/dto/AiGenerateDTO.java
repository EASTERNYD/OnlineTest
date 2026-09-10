package com.example.demo.dto;

import lombok.Data;

/**
 * AI 生成题目入参
 */
@Data
public class AiGenerateDTO {

    /** 知识点 / 出题指令 */
    private String knowledgePoint;
    /** 生成数量，默认 5 */
    private Integer count;
    /** 难度 1-简单 2-中等 3-困难，默认 1 */
    private Integer difficulty;
    /** 可选：指定题目归属分类 */
    private Long categoryId;
}
