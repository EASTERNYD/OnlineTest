package com.example.demo.vo;

import lombok.Data;

import java.util.List;

/**
 * 后台 dashboard 数据概览
 */
@Data
public class DashboardStatsVO {

    private long totalQuestions;
    private long totalExams;
    private long publishedExams;
    private long totalUsers;
    private long totalStudents;
    private long totalRecords;
    private long totalCategories;
    /** 题目难度分布 */
    private List<DifficultyStat> difficultyDistribution;
    /** 各分类题目数 */
    private List<CategoryStat> categoryDistribution;

    @Data
    public static class DifficultyStat {
        private Integer difficulty;
        private Long count;
    }

    @Data
    public static class CategoryStat {
        private Long categoryId;
        private String name;
        private Long count;
    }
}
