package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.dto.ExamComposeDTO;
import com.example.demo.dto.ExamCreateDTO;
import com.example.demo.dto.SubmitDTO;
import com.example.demo.entity.Exam;
import com.example.demo.service.ExamService;
import com.example.demo.vo.ExamDetailVO;
import com.example.demo.vo.ExamStartVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam")
@Tag(name = "试卷与考试")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/list")
    public Result<PageResult<Exam>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(examService.page(page, size));
    }

    @GetMapping("/{id}")
    @RequireAdmin
    public Result<ExamDetailVO> detail(@PathVariable Long id) {
        return Result.success(examService.detail(id));
    }

    @PostMapping
    @RequireAdmin
    public Result<Void> create(@RequestBody ExamCreateDTO dto) {
        examService.create(dto);
        return Result.success();
    }

    @PostMapping("/ai/compose")
    @RequireAdmin
    public Result<Long> compose(@RequestBody ExamComposeDTO dto) {
        return Result.success(examService.compose(dto));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> update(@PathVariable Long id, @RequestBody ExamCreateDTO dto) {
        examService.update(id, dto);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @RequireAdmin
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        examService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/start")
    public Result<ExamStartVO> start(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        return Result.success(examService.start(id, userId));
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id,
                               @RequestAttribute("userId") Long userId,
                               @RequestBody SubmitDTO dto) {
        examService.submit(userId, dto);
        return Result.success();
    }
}
