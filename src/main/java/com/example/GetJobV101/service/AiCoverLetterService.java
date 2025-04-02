package com.example.GetJobV101.service;

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
        // 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        // 2. GPT API에 보낼 메시지 구성
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 자기소개서 전문가야. 사용자의 글에서 부족한 점을 분석하고, 구체적으로 개선할 수 있는 가이드를 한국어로 알려줘."),
                        Map.of("role", "user", "content", content)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 3. GPT API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                Map.class
        );

        // 4. 응답 파싱해서 텍스트 꺼내기
        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        return (String) message.get("content");
    }


    public List<String> getInterviewQuestions(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
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

        // 줄바꿈으로 질문 나누기
        return List.of(rawText.split("\n")).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }


}
