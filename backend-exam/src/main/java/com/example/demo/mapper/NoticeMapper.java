package com.example.demo.mapper;

import com.example.demo.entity.Notice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NoticeMapper {

    @Select("SELECT * FROM notice ORDER BY create_time DESC, id DESC LIMIT #{offset}, #{size}")
    List<Notice> selectPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM notice")
    long count();

    @Select("SELECT * FROM notice WHERE status = 1 ORDER BY create_time DESC, id DESC")
    List<Notice> selectEnabled();

    @Select("SELECT * FROM notice WHERE status = 1 ORDER BY create_time DESC, id DESC LIMIT #{limit}")
    List<Notice> selectLatest(@Param("limit") int limit);

    @Select("SELECT * FROM notice WHERE id = #{id}")
    Notice selectById(Long id);

    @Insert("INSERT INTO notice(title, content, type, status, create_time) " +
            "VALUES(#{title}, #{content}, #{type}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notice notice);

    @Update("UPDATE notice SET title = #{title}, content = #{content}, type = #{type} WHERE id = #{id}")
    int update(Notice notice);

    @Update("UPDATE notice SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM notice WHERE id = #{id}")
    int delete(Long id);
}
