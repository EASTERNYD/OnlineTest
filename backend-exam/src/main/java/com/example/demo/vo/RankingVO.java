package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RankingVO {

    private Long id;
    private Long userId;
    private String nickname;
    private Long examId;
    private String examName;
    private Integer score;
    private Integer correctCount;
    private Integer wrongCount;
    private LocalDateTime submitTime;
}
