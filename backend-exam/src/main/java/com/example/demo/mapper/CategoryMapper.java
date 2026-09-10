package com.example.demo.mapper;

import com.example.demo.entity.Category;
import com.example.demo.vo.CategoryVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CategoryMapper {

    @Select("SELECT c.*, (SELECT COUNT(*) FROM question q WHERE q.category_id = c.id) AS question_count " +
            "FROM category c ORDER BY c.sort_order ASC, c.id ASC")
    List<CategoryVO> selectAllWithCount();

    @Select("SELECT * FROM category ORDER BY sort_order ASC, id ASC")
    List<Category> selectAll();

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Select("SELECT COUNT(*) FROM question WHERE category_id = #{id}")
    long countQuestionByCategory(Long id);

    @Select("SELECT * FROM category WHERE name = #{name}")
    Category findByName(String name);

    @Insert("INSERT INTO category(name, parent_id, sort_order, create_time) " +
            "VALUES(#{name}, #{parentId}, #{sortOrder}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name = #{name}, parent_id = #{parentId}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int delete(Long id);
}
