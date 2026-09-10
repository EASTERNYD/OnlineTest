package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.entity.Notice;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notice")
@Tag(name = "公告管理")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    @RequireAdmin
    public Result<PageResult<Notice>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeService.page(page, size));
    }

    @GetMapping("/enabled")
    public Result<List<Notice>> enabled() {
        return Result.success(noticeService.enabled());
    }

    @GetMapping("/latest")
    public Result<List<Notice>> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(noticeService.latest(limit));
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.success(noticeService.detail(id));
    }

    @PostMapping
    @RequireAdmin
    public Result<Void> create(@RequestBody Notice notice) {
        noticeService.create(notice);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> update(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        noticeService.update(notice);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @RequireAdmin
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        noticeService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}
