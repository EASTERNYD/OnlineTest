package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.entity.Exam;
import com.example.demo.entity.ExamRecord;
import com.example.demo.entity.Question;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.mapper.ExamRecordMapper;
import com.example.demo.mapper.QuestionMapper;
import com.example.demo.util.JsonUtil;
import com.example.demo.vo.AnswerItemVO;
import com.example.demo.vo.ExamRecordVO;
import com.example.demo.vo.RankingVO;
import com.example.demo.vo.RecordDetailVO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {

    @Autowired
    private ExamRecordMapper recordMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    public List<ExamRecordVO> list(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    public RecordDetailVO detail(Long id, Long userId) {
        ExamRecord record = requireRecord(id);
        if (!record.getUserId().equals(userId)) {
            throw new BizException(403, "无权查看该记录");
        }
        return buildDetail(record);
    }

    public void delete(Long id, Long userId) {
        ExamRecord record = requireRecord(id);
        if (!record.getUserId().equals(userId)) {
            throw new BizException(403, "无权删除该记录");
        }
        recordMapper.delete(id);
    }

    public List<RankingVO> ranking() {
        return recordMapper.ranking();
    }

    // ===== 管理员视角 =====

    public PageResult<ExamRecordVO> adminList(int page, int size, Long userId, Long examId) {
        int offset = (page - 1) * size;
        List<ExamRecordVO> list = recordMapper.selectAllPage(userId, examId, offset, size);
        long total = recordMapper.countAll(userId, examId);
        return new PageResult<>(total, list);
    }

    public RecordDetailVO adminDetail(Long id) {
        return buildDetail(requireRecord(id));
    }

    public void adminDelete(Long id) {
        recordMapper.delete(id);
    }

    // ===== 私有辅助 =====

    private ExamRecord requireRecord(Long id) {
        ExamRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BizException(404, "考试记录不存在");
        }
        return record;
    }

    private RecordDetailVO buildDetail(ExamRecord record) {
        Exam exam = examMapper.selectById(record.getExamId());
        List<Long> ids = JsonUtil.fromJson(exam == null ? null : exam.getQuestionIds(), new TypeReference<List<Long>>() {
        });
        Map<String, String> userAnswers = JsonUtil.fromJson(record.getAnswers(), new TypeReference<Map<String, String>>() {
        });

        List<AnswerItemVO> items = new ArrayList<>();
        if (ids != null) {
            for (Long qid : ids) {
                Question q = questionMapper.selectById(qid);
                if (q == null) {
                    continue;
                }
                AnswerItemVO item = new AnswerItemVO();
                item.setQuestionId(qid);
                item.setStem(q.getStem());
                item.setOptions(JsonUtil.fromJson(q.getOptions(), new TypeReference<List<String>>() {
                }));
                item.setType(q.getType());
                item.setCorrectAnswer(q.getAnswer());
                String ua = userAnswers == null ? null : userAnswers.get(String.valueOf(qid));
                item.setUserAnswer(ua);
                item.setCorrect(isCorrect(q.getAnswer(), ua));
                item.setAnalysis(q.getAnalysis());
                items.add(item);
            }
        }

        RecordDetailVO vo = new RecordDetailVO();
        vo.setId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setExamName(exam == null ? null : exam.getName());
        vo.setScore(record.getScore());
        vo.setCorrectCount(record.getCorrectCount());
        vo.setWrongCount(record.getWrongCount());
        vo.setStatus(record.getStatus());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setAnswers(items);
        return vo;
    }

    private boolean isCorrect(String correctAnswer, String userAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        return sortLetters(correctAnswer).equalsIgnoreCase(sortLetters(userAnswer));
    }

    private String sortLetters(String s) {
        char[] arr = s.toCharArray();
        Arrays.sort(arr);
        return new String(arr);
    }
}
