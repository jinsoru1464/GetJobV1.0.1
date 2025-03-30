package com.example.GetJobV101.controller;

import com.example.GetJobV101.PresignedUrlRequest;
import com.example.GetJobV101.dto.PortfolioDto;
import com.example.GetJobV101.entity.Portfolio;
import com.example.GetJobV101.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @Value("${spring.file.upload-dir}")
    private String uploadDir;

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "포트폴리오 생성", description = "DTO로 프로젝트 정보와 이미지를 업로드합니다.")
    public ResponseEntity<?> createPortfolio(
            @ModelAttribute PortfolioDto dto,
            @Parameter(description = "프로젝트 제목") @RequestParam("title") String title,
            @Parameter(description = "프로젝트 주제/설명") @RequestParam("subject") String subject,
            @Parameter(description = "시작일 (yyyy-MM-dd)") @RequestParam("startDate") String startDate,
            @Parameter(description = "종료일 (yyyy-MM-dd)") @RequestParam("endDate") String endDate,
            @Parameter(description = "팀 규모") @RequestParam("teamSize") String teamSize,
            @Parameter(description = "기술 스택 (쉼표 구분)") @RequestParam("skills") String skills,
            @Parameter(description = "담당 역할") @RequestParam("role") String role,
            @Parameter(description = "수행한 작업 내용 목록") @RequestParam("descriptions") List<String> descriptions,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            Path dirPath = Paths.get(uploadDir);
            System.out.println("✅ 업로드 경로 (절대경로): " + dirPath.toAbsolutePath());

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("✅ 업로드 디렉터리를 새로 생성했습니다.");
            }

            List<String> uploadedFileNames = new ArrayList<>();
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");


            if (images != null && images.length > 0) {
                for (MultipartFile file : images) {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.isEmpty()) {
                        continue;
                    }

                    String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                    if (!allowedExtensions.contains(extension)) {
                        throw new IOException("지원하지 않는 파일 형식: " + originalFilename);
                    }

                    String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
                    Path filePath = dirPath.resolve(uniqueFileName);
                    file.transferTo(filePath.toFile());
                    uploadedFileNames.add(uniqueFileName);
                }
            }


            dto.setImagePaths(uploadedFileNames);

            Portfolio savedPortfolio = portfolioService.savePortfolio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPortfolio);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🚩 실패 이유: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Operation(summary = "전체 포트폴리오 조회", description = "등록된 모든 포트폴리오 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "포트폴리오 리스트 반환 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class)))
    @GetMapping
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        return ResponseEntity.ok(portfolioService.getAllPortfolios());
    }

    @Operation(summary = "단일 포트폴리오 조회", description = "ID를 기반으로 포트폴리오 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "포트폴리오 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class)))
    @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long id) {
        Optional<Portfolio> portfolio = portfolioService.getPortfolioById(id);
        return portfolio.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "포트폴리오 삭제", description = "특정 ID의 포트폴리오를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "500", description = "삭제 실패")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePortfolio(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.ok("✅ 포트폴리오가 삭제되었습니다.");
    }

    @Operation(summary = "포트폴리오 수정", description = "기존 포트폴리오 정보를 수정하고 이미지를 갱신합니다.")
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updatePortfolio(
            @Parameter(description = "수정 대상 포트폴리오 ID") @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("teamSize") String teamSize,
            @RequestParam("skills") String skills,
            @RequestParam("role") String role,
            @RequestParam("descriptions") List<String> descriptions,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            // 기존 포트폴리오 가져오기
            Optional<Portfolio> existingPortfolioOpt = portfolioService.getPortfolioById(id);
            if (existingPortfolioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("🚩 포트폴리오를 찾을 수 없습니다.");
            }

            Portfolio existingPortfolio = existingPortfolioOpt.get();

            // 기존 이미지 경로 가져오기
            List<String> existingImagePaths = existingPortfolio.getImagePaths();
            if (existingImagePaths == null) {
                existingImagePaths = new ArrayList<>();
            }

            // 이미지 업로드 처리
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            List<String> uploadedFileNames = new ArrayList<>();
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

            if (images != null && images.length > 0) {
                for (MultipartFile file : images) {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.isEmpty()) {
                        continue;
                    }

                    String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                    if (!allowedExtensions.contains(extension)) {
                        throw new IOException("지원하지 않는 파일 형식: " + originalFilename);
                    }

                    String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
                    Path filePath = dirPath.resolve(uniqueFileName);
                    file.transferTo(filePath.toFile());
                    uploadedFileNames.add(uniqueFileName);
                }
            }

// ✅ 이미지가 없으면 no-image.png 기본값으로 세팅
            if (uploadedFileNames.isEmpty()) {
                uploadedFileNames.add("no-image.png");
            }

            // DTO 생성 및 업데이트
            PortfolioDto dto = new PortfolioDto(title, subject, startDate, endDate, teamSize, skills, role, descriptions, uploadedFileNames);
            Portfolio updatedPortfolio = portfolioService.updatePortfolio(id, dto);

            return ResponseEntity.ok(updatedPortfolio);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🚩 수정 실패: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Operation(summary = "Presigned URL 요청", description = "S3에 업로드할 수 있는 presigned URL을 요청합니다.")
    @PostMapping("/preSignedUrl")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        Map<String, String> preSignedUrl = portfolioService.getPresignedUrl("image", request.getImageName());
        return ResponseEntity.ok(preSignedUrl);
    }

}
