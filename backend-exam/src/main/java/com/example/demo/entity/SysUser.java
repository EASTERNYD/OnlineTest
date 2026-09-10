package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
public class SysUser {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    /** 0-管理员，1-学生 */
    private Integer role;
    /** 0-禁用，1-启用 */
    private Integer status;
    private LocalDateTime createTime;
}
