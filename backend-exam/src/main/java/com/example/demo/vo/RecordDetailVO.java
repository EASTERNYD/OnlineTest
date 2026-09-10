package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecordDetailVO {

    private Long id;
    private Long examId;
    private String examName;
    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private List<AnswerItemVO> answers;
}
