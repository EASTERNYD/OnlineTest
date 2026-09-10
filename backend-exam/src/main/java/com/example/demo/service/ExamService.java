package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.dto.ExamComposeDTO;
import com.example.demo.dto.ExamCreateDTO;
import com.example.demo.dto.SubmitDTO;
import com.example.demo.entity.Exam;
import com.example.demo.entity.ExamRecord;
import com.example.demo.entity.Question;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.mapper.ExamRecordMapper;
import com.example.demo.mapper.QuestionMapper;
import com.example.demo.util.JsonUtil;
import com.example.demo.vo.ExamDetailVO;
import com.example.demo.vo.ExamStartVO;
import com.example.demo.vo.QuestionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper recordMapper;

    public PageResult<Exam> page(int page, int size) {
        int offset = (page - 1) * size;
        List<Exam> list = examMapper.selectPage(offset, size);
        long total = examMapper.count();
        return new PageResult<>(total, list);
    }

    public ExamDetailVO detail(Long id) {
        Exam exam = requireExam(id);
        ExamDetailVO vo = new ExamDetailVO();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setTotalScore(exam.getTotalScore());
        vo.setDuration(exam.getDuration());
        vo.setStatus(exam.getStatus());
        vo.setCreateTime(exam.getCreateTime());
        vo.setQuestions(loadQuestions(parseIds(exam.getQuestionIds())));
        return vo;
    }

    public void create(ExamCreateDTO dto) {
        Exam exam = new Exam();
        exam.setName(dto.getName());
        exam.setTotalScore(dto.getTotalScore());
        exam.setDuration(dto.getDuration());
        exam.setQuestionIds(JsonUtil.toJson(dto.getQuestionIds()));
        exam.setStatus(0);
        exam.setCreateTime(LocalDateTime.now());
        examMapper.insert(exam);
    }

    public Long compose(ExamComposeDTO dto) {
        if (dto.getRules() == null || dto.getRules().isEmpty()) {
            throw new BizException("组卷规则不能为空");
        }
        Set<Long> selected = new LinkedHashSet<>();
        for (ExamComposeDTO.ComposeRule rule : dto.getRules()) {
            int count = (rule.getCount() == null || rule.getCount() < 1) ? 0 : rule.getCount();
            if (count == 0 || rule.getCategoryId() == null) {
                continue;
            }
            List<Question> picked = questionMapper.selectRandom(rule.getCategoryId(), rule.getDifficulty(), count);
            picked.removeIf(q -> selected.contains(q.getId()));
            // 指定难度抽不足时，放宽难度补齐
            if (picked.size() < count && rule.getDifficulty() != null) {
                int need = count - picked.size();
                for (Question q : questionMapper.selectRandom(rule.getCategoryId(), null, need)) {
                    if (picked.size() >= count) {
                        break;
                    }
                    if (!selected.contains(q.getId()) && !picked.contains(q)) {
                        picked.add(q);
                    }
                }
            }
            for (Question q : picked) {
                selected.add(q.getId());
            }
        }
        if (selected.isEmpty()) {
            throw new BizException("未能抽到题目，请检查题库或组卷规则");
        }
        Exam exam = new Exam();
        exam.setName(dto.getName());
        exam.setTotalScore(selected.size() * 5);
        exam.setDuration(dto.getDuration());
        exam.setQuestionIds(JsonUtil.toJson(new ArrayList<>(selected)));
        exam.setStatus(0);
        exam.setCreateTime(LocalDateTime.now());
        examMapper.insert(exam);
        return exam.getId();
    }

    public void update(Long id, ExamCreateDTO dto) {
        Exam exam = requireExam(id);
        exam.setName(dto.getName());
        exam.setTotalScore(dto.getTotalScore());
        exam.setDuration(dto.getDuration());
        exam.setQuestionIds(JsonUtil.toJson(dto.getQuestionIds()));
        examMapper.update(exam);
    }

    public void updateStatus(Long id, Integer status) {
        examMapper.updateStatus(id, status);
    }

    public void delete(Long id) {
        examMapper.delete(id);
    }

    public ExamStartVO start(Long examId, Long userId) {
        Exam exam = requireExam(examId);
        if (exam.getStatus() == null || exam.getStatus() != 1) {
            throw new BizException("试卷未发布，无法开始考试");
        }
        List<Long> ids = parseIds(exam.getQuestionIds());
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStatus(0);
        record.setStartTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        List<QuestionVO> questions = loadQuestions(ids);
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });

        ExamStartVO vo = new ExamStartVO();
        vo.setRecordId(record.getId());
        vo.setExamId(exam.getId());
        vo.setName(exam.getName());
        vo.setDuration(exam.getDuration());
        vo.setTotalScore(exam.getTotalScore());
        vo.setQuestions(questions);
        return vo;
    }

    public void submit(Long userId, SubmitDTO dto) {
        if (dto.getRecordId() == null) {
            throw new BizException("缺少 recordId");
        }
        ExamRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BizException(404, "考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该记录");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new BizException("该试卷已交卷，不能重复提交");
        }
        Exam exam = requireExam(record.getExamId());
        List<Long> ids = parseIds(exam.getQuestionIds());
        Map<String, String> answers = dto.getAnswers();

        int correct = 0;
        for (Long qid : ids) {
            Question q = questionMapper.selectById(qid);
            String ua = answers == null ? null : answers.get(String.valueOf(qid));
            if (q != null && isCorrect(q.getAnswer(), ua)) {
                correct++;
            }
        }
        int total = ids.size();
        int wrong = total - correct;
        int perScore = (total > 0 && exam.getTotalScore() != null) ? exam.getTotalScore() / total : 0;

        record.setAnswers(answers == null ? "{}" : JsonUtil.toJson(answers));
        record.setScore(correct * perScore);
        record.setCorrectCount(correct);
        record.setWrongCount(wrong);
        record.setStatus(1);
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.submit(record);
    }

    private Exam requireExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BizException(404, "试卷不存在");
        }
        return exam;
    }

    private List<Long> parseIds(String json) {
        return JsonUtil.fromJson(json, new TypeReference<List<Long>>() {
        });
    }

    private List<QuestionVO> loadQuestions(List<Long> ids) {
        List<QuestionVO> result = new ArrayList<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            Question q = questionMapper.selectById(id);
            if (q != null) {
                result.add(QuestionVO.from(q));
            }
        }
        return result;
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
