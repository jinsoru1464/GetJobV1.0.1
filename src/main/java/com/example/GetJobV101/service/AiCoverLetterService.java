package com.example.GetJobV101.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiCoverLetterService {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getGuide(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 자기소개서 전문가야. 사용자의 글에서 부족한 점을 분석하고, 구체적으로 개선할 수 있는 가이드를 한국어로 알려줘."),
                        Map.of("role", "user", "content", content)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );

        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        return (String) message.get("content");
    }

    public List<String> getInterviewQuestions(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 인사담당 면접관이야. 사용자의 자기소개서를 읽고, 그에 대해 실제 면접에서 물어볼 수 있는 질문을 5개 만들어줘. 질문은 한국어로 해줘."),
                        Map.of("role", "user", "content", content)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );

        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        String rawText = (String) message.get("content");

        return List.of(rawText.split("\n")).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    public Map<String, Object> analyzeCoverLetter(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        // 1. 역할 추론
        String rolePrompt = "다음 자기소개서를 보고 직군을 판단해 주세요. 선택지는 [개발자, 디자이너, 기획자] 중 하나입니다.\n\n" +
                "자기소개서:\n" + content + "\n\n직군:";
        Map<String, Object> roleRequestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "user", "content", rolePrompt)
                )
        );
        HttpEntity<Map<String, Object>> roleRequest = new HttpEntity<>(roleRequestBody, headers);
        String role = extractGptText(roleRequest).trim();

        // 2. 분석 요청
        String analysisPrompt = String.format("""
                당신은 %s 직군의 채용 담당자입니다. 아래 자기소개서를 읽고 분석하세요.

                출력 형식:
                [%s]

                {
                  "summary": "요약 (한 문장)",
                  "strengths": ["강점1", "강점2"],
                  "weaknesses": ["보완점1", "보완점2"],
                  "recommendations": ["추천1", "추천2"]
                }

                자기소개서:
                %s

                위 형식을 그대로 지켜서 출력하세요.
                """, role, role, content);

        Map<String, Object> analysisRequestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "user", "content", analysisPrompt)
                )
        );
        HttpEntity<Map<String, Object>> analysisRequest = new HttpEntity<>(analysisRequestBody, headers);
        String fullText = extractGptText(analysisRequest);

        String jsonPart = extractJsonFromText(fullText);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> analysisMap;
        try {
            analysisMap = objectMapper.readValue(jsonPart, Map.class);
        } catch (Exception e) {
            analysisMap = Map.of(
                    "summary", "분석 실패",
                    "strengths", List.of(),
                    "weaknesses", List.of(),
                    "recommendations", List.of()
            );
        }

        return Map.of(
                "role", role,
                "analysis", analysisMap
        );
    }

    private String extractGptText(HttpEntity<Map<String, Object>> request) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );
        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        return (String) message.get("content");
    }

    private String extractJsonFromText(String fullText) {
        int start = fullText.indexOf("{");
        int end = fullText.lastIndexOf("}") + 1;
        if (start != -1 && end != -1 && end > start) {
            return fullText.substring(start, end);
        }
        return "{}";
    }
}
