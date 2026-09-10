package com.example.demo.controller;

import com.example.demo.common.RequireAdmin;
import com.example.demo.common.Result;
import com.example.demo.entity.Category;
import com.example.demo.service.CategoryService;
import com.example.demo.vo.CategoryVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@Tag(name = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @PostMapping
    @RequireAdmin
    public Result<Void> create(@RequestBody Category category) {
        categoryService.create(category);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
