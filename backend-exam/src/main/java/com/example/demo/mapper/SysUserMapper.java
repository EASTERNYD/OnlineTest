package com.example.demo.mapper;

import com.example.demo.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser selectById(Long id);

    @Insert("INSERT INTO sys_user(username, password, nickname, role, status, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{role}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);
}
