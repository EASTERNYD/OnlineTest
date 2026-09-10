package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.entity.Category;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryVO> list() {
        return categoryMapper.selectAllWithCount();
    }

    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectAll();
        Map<Long, CategoryVO> map = new HashMap<>();
        List<CategoryVO> roots = new ArrayList<>();

        for (Category c : all) {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            vo.setChildren(new ArrayList<>());
            map.put(c.getId(), vo);
        }

        for (CategoryVO vo : map.values()) {
            Long pid = vo.getParentId();
            if (pid == null || pid == 0 || !map.containsKey(pid)) {
                roots.add(vo);
            } else {
                map.get(pid).getChildren().add(vo);
            }
        }
        return roots;
    }

    public void create(Category category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.update(category);
    }

    public void delete(Long id) {
        long count = categoryMapper.countQuestionByCategory(id);
        if (count > 0) {
            throw new BizException("该分类下存在题目，无法删除");
        }
        categoryMapper.delete(id);
    }
}
