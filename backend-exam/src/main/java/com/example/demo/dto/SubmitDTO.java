package com.example.demo.dto;

import lombok.Data;

import java.util.Map;

/**
 * 提交答案入参
 */
@Data
public class SubmitDTO {

    private Long recordId;
    /** key: 题目ID(字符串)，value: 考生答案 "A" 或 "ABD" */
    private Map<String, String> answers;
}
