package com.example.demo.service;

import com.example.demo.common.BizException;
import com.example.demo.dto.AiGenerateDTO;
import com.example.demo.util.JsonUtil;
import com.example.demo.vo.QuestionVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 生成题目服务（调用 DeepSeek，OpenAI 兼容协议）
 */
@Service
public class AiService {

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    private final RestClient restClient = RestClient.create();

    private static final String SYSTEM_PROMPT = "你是一名专业的考试出题专家，擅长出单项选择题。请严格只返回 JSON，不要输出任何多余文字或 Markdown 代码块。";

    public List<QuestionVO> generate(AiGenerateDTO dto) {
        if (dto.getKnowledgePoint() == null || dto.getKnowledgePoint().trim().isEmpty()) {
            throw new BizException("知识点不能为空");
        }
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("sk-xxx")) {
            throw new BizException("请先在 application.yml 中配置 llm.api-key（DeepSeek API Key）");
        }
        int count = (dto.getCount() == null || dto.getCount() < 1) ? 5 : dto.getCount();
        int difficulty = dto.getDifficulty() == null ? 1 : dto.getDifficulty();

        String userPrompt = "请根据知识点「" + dto.getKnowledgePoint() + "」生成 " + count + " 道单项选择题，难度为"
                + difficultyText(difficulty) + "。每题 4 个选项，且只有一个正确答案。严格返回如下 JSON 结构："
                + "{\"questions\":[{\"stem\":\"题干\",\"options\":[\"A. xxx\",\"B. xxx\",\"C. xxx\",\"D. xxx\"],\"answer\":\"A\",\"analysis\":\"解析\",\"difficulty\":" + difficulty + "}]}";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.7);

        String resp;
        try {
            resp = restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new BizException("调用大模型失败: " + e.getMessage());
        }

        return parseResponse(resp, dto.getCategoryId());
    }

    private List<QuestionVO> parseResponse(String resp, Long categoryId) {
        ChatResponse chatResp = JsonUtil.fromJson(resp, ChatResponse.class);
        if (chatResp == null || chatResp.getChoices() == null || chatResp.getChoices().isEmpty()) {
            throw new BizException("大模型返回结果为空");
        }
        String content = chatResp.getChoices().get(0).getMessage().getContent();
        content = stripCodeFence(content);
        Map<String, List<AiQuestion>> map = JsonUtil.fromJson(content, new TypeReference<Map<String, List<AiQuestion>>>() {
        });
        if (map == null || map.get("questions") == null || map.get("questions").isEmpty()) {
            throw new BizException("大模型返回格式异常，未解析到题目");
        }
        return map.get("questions").stream().map(q -> toVO(q, categoryId)).collect(Collectors.toList());
    }

    private QuestionVO toVO(AiQuestion q, Long categoryId) {
        QuestionVO vo = new QuestionVO();
        vo.setStem(q.getStem());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setDifficulty(q.getDifficulty() == null ? 1 : q.getDifficulty());
        vo.setType(0);
        vo.setCategoryId(categoryId);
        return vo;
    }

    private String stripCodeFence(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl >= 0) {
                t = t.substring(nl + 1);
            }
            if (t.endsWith("```")) {
                t = t.substring(0, t.length() - 3);
            }
        }
        return t.trim();
    }

    private String difficultyText(int d) {
        return switch (d) {
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "简单";
        };
    }

    @Data
    public static class ChatResponse {
        private List<Choice> choices;
    }

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String content;
    }

    @Data
    public static class AiQuestion {
        private String stem;
        private List<String> options;
        private String answer;
        private String analysis;
        private Integer difficulty;
    }
}
