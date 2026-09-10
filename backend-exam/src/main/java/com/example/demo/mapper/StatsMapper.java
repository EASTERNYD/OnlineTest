package com.example.demo.mapper;

import com.example.demo.vo.DashboardStatsVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM question")
    long countQuestions();

    @Select("SELECT COUNT(*) FROM exam")
    long countExams();

    @Select("SELECT COUNT(*) FROM exam WHERE status = 1")
    long countPublishedExams();

    @Select("SELECT COUNT(*) FROM sys_user")
    long countUsers();

    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 1")
    long countStudents();

    @Select("SELECT COUNT(*) FROM exam_record WHERE status = 1")
    long countRecords();

    @Select("SELECT COUNT(*) FROM category")
    long countCategories();

    @Select("SELECT difficulty, COUNT(*) AS count FROM question GROUP BY difficulty ORDER BY difficulty")
    List<DashboardStatsVO.DifficultyStat> countByDifficulty();

    @Select("SELECT c.id AS category_id, c.name AS name, COUNT(q.id) AS count " +
            "FROM category c LEFT JOIN question q ON q.category_id = c.id " +
            "GROUP BY c.id, c.name ORDER BY c.sort_order, c.id")
    List<DashboardStatsVO.CategoryStat> countByCategory();
}
