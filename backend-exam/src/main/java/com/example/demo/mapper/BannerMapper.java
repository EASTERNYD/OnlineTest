package com.example.demo.mapper;

import com.example.demo.entity.Banner;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BannerMapper {

    @Select("SELECT * FROM banner ORDER BY sort_order DESC, id DESC LIMIT #{offset}, #{size}")
    List<Banner> selectPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM banner")
    long count();

    @Select("SELECT * FROM banner WHERE status = 1 ORDER BY sort_order DESC, id DESC")
    List<Banner> selectEnabled();

    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner selectById(Long id);

    @Insert("INSERT INTO banner(title, image_url, link_url, sort_order, status, create_time) " +
            "VALUES(#{title}, #{imageUrl}, #{linkUrl}, #{sortOrder}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);

    @Update("UPDATE banner SET title = #{title}, image_url = #{imageUrl}, link_url = #{linkUrl}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(Banner banner);

    @Update("UPDATE banner SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM banner WHERE id = #{id}")
    int delete(Long id);
}
