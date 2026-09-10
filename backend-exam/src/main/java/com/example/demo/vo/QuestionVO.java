package com.example.demo.vo;

import com.example.demo.entity.Question;
import com.example.demo.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {

    private Long id;
    private Long categoryId;
    private String stem;
    /** 选项列表（已从 JSON 解析） */
    private List<String> options;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Integer viewCount;
    private Integer type;
    private LocalDateTime createTime;

    public static QuestionVO from(Question q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setCategoryId(q.getCategoryId());
        vo.setStem(q.getStem());
        vo.setOptions(JsonUtil.fromJson(q.getOptions(), new TypeReference<List<String>>() {
        }));
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setDifficulty(q.getDifficulty());
        vo.setViewCount(q.getViewCount());
        vo.setType(q.getType());
        vo.setCreateTime(q.getCreateTime());
        return vo;
    }
}
