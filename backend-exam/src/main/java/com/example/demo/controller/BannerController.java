package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.entity.Banner;
import com.example.demo.service.BannerService;
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
@RequestMapping("/api/v1/banner")
@Tag(name = "轮播图管理")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    @RequireAdmin
    public Result<PageResult<Banner>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(bannerService.page(page, size));
    }

    @GetMapping("/enabled")
    public Result<List<Banner>> enabled() {
        return Result.success(bannerService.enabled());
    }

    @GetMapping("/{id}")
    public Result<Banner> detail(@PathVariable Long id) {
        return Result.success(bannerService.detail(id));
    }

    @PostMapping
    @RequireAdmin
    public Result<Void> create(@RequestBody Banner banner) {
        bannerService.create(banner);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.update(banner);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    @RequireAdmin
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bannerService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}
