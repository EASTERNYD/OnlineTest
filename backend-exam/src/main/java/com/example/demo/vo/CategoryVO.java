package com.example.demo.vo;

import com.example.demo.entity.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryVO extends Category {

    /** 题目数量统计 */
    private Long questionCount;
    /** 树形子节点 */
    private List<CategoryVO> children;
}
