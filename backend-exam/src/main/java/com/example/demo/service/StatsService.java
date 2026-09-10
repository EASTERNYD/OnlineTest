package com.example.demo.service;

import com.example.demo.mapper.StatsMapper;
import com.example.demo.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private StatsMapper statsMapper;

    public DashboardStatsVO dashboard() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setTotalQuestions(statsMapper.countQuestions());
        vo.setTotalExams(statsMapper.countExams());
        vo.setPublishedExams(statsMapper.countPublishedExams());
        vo.setTotalUsers(statsMapper.countUsers());
        vo.setTotalStudents(statsMapper.countStudents());
        vo.setTotalRecords(statsMapper.countRecords());
        vo.setTotalCategories(statsMapper.countCategories());
        vo.setDifficultyDistribution(statsMapper.countByDifficulty());
        vo.setCategoryDistribution(statsMapper.countByCategory());
        return vo;
    }
}
