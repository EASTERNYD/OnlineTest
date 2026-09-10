package com.example.demo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 题目导入/模板 行模型（表头与模板一致）
 */
@Data
public class QuestionImportVO {

    @ExcelProperty("题干")
    private String stem;

    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String analysis;

    @ExcelProperty("难度")
    private Integer difficulty;

    @ExcelProperty("分类")
    private String categoryName;
}
