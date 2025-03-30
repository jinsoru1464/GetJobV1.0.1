package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포트폴리오 작성 및 수정 시 사용되는 데이터 전송 객체")
public class PortfolioDto {

    @Schema(description = "프로젝트의 제목", example = "AI 기반 이력서 생성 웹앱")
    private String title;

    @Schema(description = "프로젝트 주제 또는 한 줄 요약", example = "Spring Boot를 활용한 백엔드 개발")
    private String subject;

    @Schema(description = "프로젝트 시작일 (yyyy-MM-dd 형식의 문자열)", example = "2023-01-01")
    private String startDate;

    @Schema(description = "프로젝트 종료일 (yyyy-MM-dd 형식의 문자열)", example = "2023-03-31")
    private String endDate;

    @Schema(description = "팀 규모 또는 참여 인원 수", example = "4명")
    private String teamSize;

    @Schema(description = "사용한 기술 스택 (쉼표로 구분된 문자열)", example = "Java, Spring Boot, MySQL, JPA")
    private String skills;

    @Schema(description = "자신의 역할", example = "개발자, PM, 디자인 중 하나")
    private String role;

    @Schema(description = "프로젝트에서 수행한 작업 또는 기능 상세 설명 리스트", example = "[\"ERD 설계 및 구축\", \"JWT 인증 기반 로그인 구현\"]")
    private List<String> descriptions;

    @Schema(description = "이미지 파일 경로 리스트. S3에 저장된 이미지 URL 또는 경로 문자열", example = "[\"/images/portfolio/cover.png\", \"/images/portfolio/detail.png\"]")
    private List<String> imagePaths;
}
