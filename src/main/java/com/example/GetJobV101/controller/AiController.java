package com.example.GetJobV101.controller;

import com.example.GetJobV101.dto.CoverLetterRequest;
import com.example.GetJobV101.service.AiCoverLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 자기소개서", description = "ChatGPT를 이용한 자기소개서 자동 생성 기능")
public class AiController {

    private final AiCoverLetterService aiCoverLetterService;

    @PostMapping("/cover-letter-guide")
    @Operation(
            summary = "AI 자기소개서 문장 가이드 요청",
            description = "입력된 자기소개서를 바탕으로 ChatGPT를 활용해 보완 가이드를 생성합니다.",
            requestBody = @RequestBody(
                    description = "자기소개서 내용",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "AI 가이드 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "자기소개서가 비어 있음")
            }
    )
    public ResponseEntity<Map<String, String>> getCoverLetterGuide(
            @org.springframework.web.bind.annotation.RequestBody CoverLetterRequest request
    ) {
        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("guide", "❗자기소개서를 입력해주세요."));
        }
        String guide = aiCoverLetterService.getGuide(content);
        return ResponseEntity.ok(Map.of("guide", guide));
    }

    @PostMapping("/interview-questions")
    @Operation(
            summary = "AI 면접 질문 생성",
            description = "입력된 자기소개서를 바탕으로 ChatGPT를 활용해 예상 면접 질문을 생성합니다.",
            requestBody = @RequestBody(
                    description = "자기소개서 내용",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "면접 질문 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "자기소개서가 비어 있음")
            }
    )
    public ResponseEntity<Map<String, Object>> getInterviewQuestions(
            @org.springframework.web.bind.annotation.RequestBody CoverLetterRequest request
    ) {
        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("questions", List.of("❗자기소개서를 입력해주세요.")));
        }
        List<String> questions = aiCoverLetterService.getInterviewQuestions(content);
        return ResponseEntity.ok(Map.of("questions", questions));
    }




}