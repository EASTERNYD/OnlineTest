package com.example.demo.mapper;

import com.example.demo.entity.Exam;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ExamMapper {

    @Select("SELECT * FROM exam ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<Exam> selectPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM exam")
    long count();

    @Select("SELECT * FROM exam WHERE id = #{id}")
    Exam selectById(Long id);

    @Insert("INSERT INTO exam(name, total_score, duration, question_ids, status, create_time) " +
            "VALUES(#{name}, #{totalScore}, #{duration}, #{questionIds}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Exam exam);

    @Update("UPDATE exam SET name = #{name}, total_score = #{totalScore}, duration = #{duration}, question_ids = #{questionIds} WHERE id = #{id}")
    int update(Exam exam);

    @Update("UPDATE exam SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM exam WHERE id = #{id}")
    int delete(Long id);
}
