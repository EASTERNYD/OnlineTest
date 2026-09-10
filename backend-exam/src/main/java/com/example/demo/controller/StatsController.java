package com.example.demo.controller;

import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.service.StatsService;
import com.example.demo.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "数据统计")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/dashboard")
    @RequireAdmin
    public Result<DashboardStatsVO> dashboard() {
        return Result.success(statsService.dashboard());
    }
}
