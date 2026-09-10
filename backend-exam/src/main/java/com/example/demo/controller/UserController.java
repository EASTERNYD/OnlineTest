package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.SysUser;
import com.example.demo.service.UserService;
import com.example.demo.vo.LoginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<SysUser> info(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.info(userId));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
