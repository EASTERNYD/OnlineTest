package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.dto.QuestionImportVO;
import com.example.demo.dto.QuestionQueryDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Question;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.QuestionMapper;
import com.example.demo.util.ExcelUtil;
import com.example.demo.util.JsonUtil;
import com.example.demo.vo.QuestionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    public PageResult<QuestionVO> page(QuestionQueryDTO dto) {
        int page = (dto.getPage() == null || dto.getPage() < 1) ? 1 : dto.getPage();
        int size = (dto.getSize() == null || dto.getSize() < 1) ? 10 : dto.getSize();
        int offset = (page - 1) * size;
        List<Question> list = questionMapper.selectPage(dto.getCategoryId(), dto.getDifficulty(), dto.getType(), dto.getKeyword(), offset, size);
        long total = questionMapper.countPage(dto.getCategoryId(), dto.getDifficulty(), dto.getType(), dto.getKeyword());
        List<QuestionVO> voList = list.stream().map(QuestionVO::from).collect(Collectors.toList());
        return new PageResult<>(total, voList);
    }

    public List<QuestionVO> hot(int limit) {
        return questionMapper.selectHot(limit).stream().map(QuestionVO::from).collect(Collectors.toList());
    }

    public QuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BizException(404, "题目不存在");
        }
        questionMapper.incrementViewCount(id);
        q.setViewCount((q.getViewCount() == null ? 0 : q.getViewCount()) + 1);
        return QuestionVO.from(q);
    }

    public void create(Question q) {
        if (q.getType() == null) {
            q.setType(0);
        }
        if (q.getDifficulty() == null) {
            q.setDifficulty(1);
        }
        if (q.getViewCount() == null) {
            q.setViewCount(0);
        }
        q.setCreateTime(LocalDateTime.now());
        questionMapper.insert(q);
    }

    public void update(Question q) {
        if (questionMapper.selectById(q.getId()) == null) {
            throw new BizException(404, "题目不存在");
        }
        questionMapper.update(q);
    }

    public void delete(Long id) {
        questionMapper.delete(id);
    }

    public List<QuestionImportVO> preview(MultipartFile file) {
        return ExcelUtil.read(file, QuestionImportVO.class);
    }

    public int importExcel(MultipartFile file) {
        List<QuestionImportVO> list = ExcelUtil.read(file, QuestionImportVO.class);
        List<Question> questions = convert(list);
        if (questions.isEmpty()) {
            return 0;
        }
        return questionMapper.batchInsert(questions);
    }

    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtil.writeTemplate(response, QuestionImportVO.class, "题目导入模板");
    }

    private List<Question> convert(List<QuestionImportVO> list) {
        List<Question> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (QuestionImportVO vo : list) {
            if (vo.getStem() == null || vo.getStem().trim().isEmpty()) {
                continue;
            }
            Question q = new Question();
            q.setStem(vo.getStem());
            q.setOptions(buildOptions(vo));
            q.setAnswer(vo.getAnswer());
            q.setAnalysis(vo.getAnalysis());
            q.setDifficulty(vo.getDifficulty() == null ? 1 : vo.getDifficulty());
            q.setType(0);
            q.setViewCount(0);
            q.setCategoryId(resolveCategory(vo.getCategoryName()));
            q.setCreateTime(now);
            result.add(q);
        }
        return result;
    }

    private String buildOptions(QuestionImportVO vo) {
        List<String> opts = new ArrayList<>();
        addOption(opts, "A", vo.getOptionA());
        addOption(opts, "B", vo.getOptionB());
        addOption(opts, "C", vo.getOptionC());
        addOption(opts, "D", vo.getOptionD());
        return JsonUtil.toJson(opts);
    }

    private void addOption(List<String> opts, String letter, String text) {
        if (text != null && !text.trim().isEmpty()) {
            opts.add(letter + ". " + text.trim());
        }
    }

    private Long resolveCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String trimmed = name.trim();
        Category c = categoryMapper.findByName(trimmed);
        if (c != null) {
            return c.getId();
        }
        Category newC = new Category();
        newC.setName(trimmed);
        newC.setParentId(0L);
        newC.setSortOrder(0);
        newC.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(newC);
        return newC.getId();
    }
}
