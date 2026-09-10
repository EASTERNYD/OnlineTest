package com.example.demo.mapper;

import com.example.demo.entity.ExamRecord;
import com.example.demo.vo.ExamRecordVO;
import com.example.demo.vo.RankingVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ExamRecordMapper {

    @Select("SELECT r.id, r.user_id, r.exam_id, r.score, r.correct_count, r.wrong_count, r.status, " +
            "r.start_time, r.submit_time, r.create_time, e.name AS exam_name " +
            "FROM exam_record r LEFT JOIN exam e ON r.exam_id = e.id " +
            "WHERE r.user_id = #{userId} ORDER BY r.id DESC")
    List<ExamRecordVO> selectByUserId(Long userId);

    @Select("""
            <script>
            SELECT r.id, r.user_id, u.nickname AS nickname, u.username AS username, r.exam_id, e.name AS exam_name,
                   r.score, r.correct_count, r.wrong_count, r.status, r.start_time, r.submit_time, r.create_time
            FROM exam_record r
            LEFT JOIN exam e ON r.exam_id = e.id
            LEFT JOIN sys_user u ON r.user_id = u.id
            WHERE 1 = 1
            <if test="userId != null"> AND r.user_id = #{userId}</if>
            <if test="examId != null"> AND r.exam_id = #{examId}</if>
            ORDER BY r.id DESC LIMIT #{offset}, #{size}
            </script>
            """)
    List<ExamRecordVO> selectAllPage(@Param("userId") Long userId,
                                     @Param("examId") Long examId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM exam_record r WHERE 1 = 1
            <if test="userId != null"> AND r.user_id = #{userId}</if>
            <if test="examId != null"> AND r.exam_id = #{examId}</if>
            </script>
            """)
    long countAll(@Param("userId") Long userId, @Param("examId") Long examId);

    @Select("SELECT * FROM exam_record WHERE id = #{id}")
    ExamRecord selectById(Long id);

    @Insert("INSERT INTO exam_record(user_id, exam_id, answers, score, correct_count, wrong_count, status, start_time, submit_time, create_time) " +
            "VALUES(#{userId}, #{examId}, #{answers}, #{score}, #{correctCount}, #{wrongCount}, #{status}, #{startTime}, #{submitTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamRecord record);

    @Update("UPDATE exam_record SET answers = #{answers}, score = #{score}, correct_count = #{correctCount}, " +
            "wrong_count = #{wrongCount}, status = #{status}, submit_time = #{submitTime} WHERE id = #{id}")
    int submit(ExamRecord record);

    @Delete("DELETE FROM exam_record WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT r.id, r.user_id, u.nickname AS nickname, r.exam_id, e.name AS exam_name, " +
            "r.score, r.correct_count, r.wrong_count, r.submit_time " +
            "FROM exam_record r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN exam e ON r.exam_id = e.id " +
            "WHERE r.status = 1 ORDER BY r.score DESC, r.correct_count DESC")
    List<RankingVO> ranking();
}
