package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.dto.AiGenerateDTO;
import com.example.demo.dto.QuestionImportVO;
import com.example.demo.dto.QuestionQueryDTO;
import com.example.demo.entity.Question;
import com.example.demo.service.AiService;
import com.example.demo.service.QuestionService;
import com.example.demo.vo.QuestionVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question")
@Tag(name = "题目管理")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiService aiService;

    @PostMapping("/page")
    public Result<PageResult<QuestionVO>> page(@RequestBody QuestionQueryDTO dto) {
        return Result.success(questionService.page(dto));
    }

    @GetMapping("/hot")
    public Result<List<QuestionVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(questionService.hot(limit));
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        return Result.success(questionService.detail(id));
    }

    @PostMapping
    @RequireAdmin
    public Result<Void> create(@RequestBody Question question) {
        questionService.create(question);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.update(question);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }

    @PostMapping("/excel/preview")
    @RequireAdmin
    public Result<List<QuestionImportVO>> preview(@RequestParam("file") MultipartFile file) {
        return Result.success(questionService.preview(file));
    }

    @PostMapping("/excel/import")
    @RequireAdmin
    public Result<Integer> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(questionService.importExcel(file));
    }

    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        questionService.downloadTemplate(response);
    }

    @PostMapping("/ai/generate")
    @RequireAdmin
    public Result<List<QuestionVO>> aiGenerate(@RequestBody AiGenerateDTO dto) {
        return Result.success(aiService.generate(dto));
    }
}
