package com.example.demo.mapper;

import com.example.demo.entity.Question;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface QuestionMapper {

    @Select("""
            <script>
            SELECT * FROM question WHERE 1 = 1
            <if test="categoryId != null"> AND category_id = #{categoryId}</if>
            <if test="difficulty != null"> AND difficulty = #{difficulty}</if>
            <if test="type != null"> AND type = #{type}</if>
            <if test="keyword != null and keyword != ''"> AND stem LIKE CONCAT('%', #{keyword}, '%')</if>
            ORDER BY id DESC LIMIT #{offset}, #{size}
            </script>
            """)
    List<Question> selectPage(@Param("categoryId") Long categoryId,
                              @Param("difficulty") Integer difficulty,
                              @Param("type") Integer type,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM question WHERE 1 = 1
            <if test="categoryId != null"> AND category_id = #{categoryId}</if>
            <if test="difficulty != null"> AND difficulty = #{difficulty}</if>
            <if test="type != null"> AND type = #{type}</if>
            <if test="keyword != null and keyword != ''"> AND stem LIKE CONCAT('%', #{keyword}, '%')</if>
            </script>
            """)
    long countPage(@Param("categoryId") Long categoryId,
                   @Param("difficulty") Integer difficulty,
                   @Param("type") Integer type,
                   @Param("keyword") String keyword);

    @Select("SELECT * FROM question ORDER BY view_count DESC, id DESC LIMIT #{limit}")
    List<Question> selectHot(@Param("limit") int limit);

    @Select("""
            <script>
            SELECT * FROM question WHERE category_id = #{categoryId}
            <if test="difficulty != null"> AND difficulty = #{difficulty}</if>
            ORDER BY RAND() LIMIT #{limit}
            </script>
            """)
    List<Question> selectRandom(@Param("categoryId") Long categoryId,
                                @Param("difficulty") Integer difficulty,
                                @Param("limit") int limit);

    @Select("SELECT * FROM question WHERE id = #{id}")
    Question selectById(Long id);

    @Update("UPDATE question SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Insert("INSERT INTO question(category_id, stem, options, answer, analysis, difficulty, view_count, type, create_time) " +
            "VALUES(#{categoryId}, #{stem}, #{options}, #{answer}, #{analysis}, #{difficulty}, #{viewCount}, #{type}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question question);

    @Update("UPDATE question SET category_id = #{categoryId}, stem = #{stem}, options = #{options}, answer = #{answer}, " +
            "analysis = #{analysis}, difficulty = #{difficulty}, type = #{type} WHERE id = #{id}")
    int update(Question question);

    @Delete("DELETE FROM question WHERE id = #{id}")
    int delete(Long id);

    @Insert("""
            <script>
            INSERT INTO question(category_id, stem, options, answer, analysis, difficulty, view_count, type, create_time) VALUES
            <foreach collection="list" item="q" separator=",">
            (#{q.categoryId}, #{q.stem}, #{q.options}, #{q.answer}, #{q.analysis}, #{q.difficulty}, 0, #{q.type}, #{q.createTime})
            </foreach>
            </script>
            """)
    int batchInsert(@Param("list") List<Question> list);
}
