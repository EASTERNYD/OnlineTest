package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.service.RecordService;
import com.example.demo.vo.ExamRecordVO;
import com.example.demo.vo.RankingVO;
import com.example.demo.vo.RecordDetailVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/record")
@Tag(name = "考试记录与排行")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @GetMapping("/list")
    public Result<List<ExamRecordVO>> list(@RequestAttribute("userId") Long userId) {
        return Result.success(recordService.list(userId));
    }

    @GetMapping("/{id}")
    public Result<RecordDetailVO> detail(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        return Result.success(recordService.detail(id, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        recordService.delete(id, userId);
        return Result.success();
    }

    @GetMapping("/ranking")
    public Result<List<RankingVO>> ranking() {
        return Result.success(recordService.ranking());
    }

    // ===== 管理员视角：查看所有学生记录 =====

    @GetMapping("/admin/list")
    @RequireAdmin
    public Result<PageResult<ExamRecordVO>> adminList(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) Long examId) {
        return Result.success(recordService.adminList(page, size, userId, examId));
    }

    @GetMapping("/admin/{id}")
    @RequireAdmin
    public Result<RecordDetailVO> adminDetail(@PathVariable Long id) {
        return Result.success(recordService.adminDetail(id));
    }

    @DeleteMapping("/admin/{id}")
    @RequireAdmin
    public Result<Void> adminDelete(@PathVariable Long id) {
        recordService.adminDelete(id);
        return Result.success();
    }
}
