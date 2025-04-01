package com.example.GetJobV101.controller;

import com.example.GetJobV101.PresignedUrlRequest;
import com.example.GetJobV101.dto.PortfolioDto;
import com.example.GetJobV101.entity.Portfolio;
import com.example.GetJobV101.entity.User;
import com.example.GetJobV101.jwt.JwtUtil;
import com.example.GetJobV101.service.PortfolioService;
import com.example.GetJobV101.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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

    @Value("${spring.file.upload-dir:/home/ubuntu/uploads}")
    private String uploadDir;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractLoginId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.getLoginIdFromToken(header.substring(7));
        }
        return null;
    }

    @Operation(
            summary = "포트폴리오 생성",
            description = "포트폴리오를 생성하고, 관련 이미지도 업로드합니다. 역할에 따라 프로젝트 설명을 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "포트폴리오 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 입력 또는 파일 형식 불일치", content = @Content(mediaType = "application/json"))
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPortfolio(
            HttpServletRequest request,
            @ModelAttribute PortfolioDto dto,
            @Parameter(description = "프로젝트 제목") @RequestParam("title") String title,
            @Parameter(description = "프로젝트 주제/설명") @RequestParam("subject") String subject,
            @Parameter(description = "시작일 (yyyy-MM-dd)") @RequestParam("startDate") String startDate,
            @Parameter(description = "종료일 (yyyy-MM-dd)") @RequestParam("endDate") String endDate,
            @Parameter(description = "팀 규모") @RequestParam("teamSize") String teamSize,
            @Parameter(description = "기술 스택 (쉼표 구분)") @RequestParam("skills") String skills,@Parameter(
                    description = """
    사용자 역할. 해당 값에 따라 'descriptions' 필드 내용이 달라집니다.
    - developer: 개발자
    - pm: 기획자
    - designer: 디자이너
    """
            )
            @RequestParam("role") String role,

            @Parameter(
                    description = """
    역할에 따라 다음 순서대로 작성된 설명입니다. descriptions[0] ~ descriptions[5]의 의미:

    ✅ developer:
    1. 프로젝트 개요
    2. 주요 역할과 기여 사항
    3. 문제와 해결 과정
    4. 사용 기술 및 도구
    5. 피드백
    6. 관련 자료 또는 링크

    ✅ pm:
    1. 프로젝트 개요
    2. 주요 역할과 기여 사항
    3. 기획 의도 및 가치
    4. 문제 해결과 갈등 관리
    5. 피드백
    6. 관련 자료 또는 링크

    ✅ designer:
    1. 프로젝트 개요
    2. 주요 역할과 기여 사항
    3. 기획 의도 및 문제해결
    4. 사용 도구 및 기술
    5. 피드백
    6. 관련 자료 또는 링크
    """
            )
            @RequestParam("descriptions") List<String> descriptions,

            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            String loginId = extractLoginId(request);
            User user = userService.findByLoginId(loginId);

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
            dto.setUser(user); // ✅ 사용자 설정

            Portfolio savedPortfolio = portfolioService.savePortfolio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPortfolio);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🚩 실패 이유: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Operation(
            summary = "전체 포트폴리오 조회",
            description = "로그인한 사용자의 포트폴리오만 조회 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포트폴리오 목록 반환 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class))),
                    @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
            }
    )
    @GetMapping
    public ResponseEntity<List<Portfolio>> getMyPortfolios(HttpServletRequest request) {
        String loginId = extractLoginId(request);
        User user = userService.findByLoginId(loginId);
        return ResponseEntity.ok(portfolioService.getPortfoliosByUser(user));
    }

    @Operation(
            summary = "단일 포트폴리오 조회",
            description = "포트폴리오 ID를 기반으로 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포트폴리오 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class))),
                    @ApiResponse(responseCode = "404", description = "해당 포트폴리오를 찾을 수 없음")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long id, HttpServletRequest request) {
        Optional<Portfolio> portfolio = portfolioService.getPortfolioById(id);
        return portfolio.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(
            summary = "포트폴리오 삭제",
            description = "특정 ID의 포트폴리오를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포트폴리오 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "해당 포트폴리오를 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "삭제 권한이 없음")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePortfolio(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long id,
            HttpServletRequest request) {

        try {
            String loginId = extractLoginId(request);
            User user = userService.findByLoginId(loginId);

            Optional<Portfolio> portfolioOpt = portfolioService.getPortfolioById(id);
            if (portfolioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("🚫 해당 포트폴리오를 찾을 수 없습니다.");
            }

            Portfolio portfolio = portfolioOpt.get();

            if (!portfolio.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("🚫 삭제 권한이 없습니다.");
            }

            portfolioService.deletePortfolio(id);
            return ResponseEntity.ok("✅ 포트폴리오가 삭제되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("🚩 삭제 실패: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Operation(
            summary = "포트폴리오 수정",
            description = "기존의 포트폴리오 정보를 수정하고 이미지를 갱신합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "포트폴리오 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Portfolio.class))),
                    @ApiResponse(responseCode = "404", description = "해당 포트폴리오를 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "수정 권한이 없음")
            }
    )
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updatePortfolio(
            @PathVariable Long id,
            HttpServletRequest request,
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
            String loginId = extractLoginId(request);
            User user = userService.findByLoginId(loginId);

            Optional<Portfolio> existingPortfolioOpt = portfolioService.getPortfolioById(id);
            if (existingPortfolioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("🚩 포트폴리오를 찾을 수 없습니다.");
            }

            Portfolio existingPortfolio = existingPortfolioOpt.get();

            if (!existingPortfolio.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("🚫 수정 권한이 없습니다.");
            }

            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            List<String> uploadedFileNames = new ArrayList<>();
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

            if (images != null && images.length > 0) {
                for (MultipartFile file : images) {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || originalFilename.isEmpty()) continue;

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
        return ResponseEntity.ok(preSignedUrl)  ;
    }

}