        package com.example.GetJobV101.controller;

        import com.amazonaws.services.s3.AmazonS3;
        import com.amazonaws.services.s3.model.ObjectMetadata;
        import com.example.GetJobV101.dto.*;
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
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.MediaType;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.core.annotation.AuthenticationPrincipal;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

        import java.io.IOException;
        import java.time.LocalDate;
        import java.util.*;
        import java.util.stream.Collectors;

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

            @Autowired
            private AmazonS3 amazonS3;

            @Value("${spring.cloud.aws.s3.bucket}")
            private String bucket;

            @Value("${spring.cloud.aws.region.static}")
            private String region;





            @Operation(
                    summary = "포트폴리오 생성",
                    description = "포트폴리오를 생성하고, 관련 이미지도 업로드합니다.",
                    responses = {
                            @ApiResponse(responseCode = "201", description = "포트폴리오 생성 성공", content = @Content(schema = @Schema(implementation = Portfolio.class))),
                            @ApiResponse(responseCode = "400", description = "잘못된 입력 또는 파일 형식 불일치")
                    }
            )
            @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
            public ResponseEntity<?> createPortfolio(

                    @Parameter(
                            description = "포트폴리오 JSON 본문",
                            required = true,
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PortfolioDto.class)
                            )
                    )
                    @RequestPart("portfolio") PortfolioDto dto,

                    @Parameter(
                            description = "이미지 파일들 (선택)",
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    )
                    @RequestPart(value = "images", required = false) MultipartFile[] images,

                    @AuthenticationPrincipal UserDetails userDetails
            )

             {
                try {
                    String loginId = userDetails.getUsername();
                    User user = userService.findByLoginId(loginId);

                    List<String> imageUrls = new ArrayList<>();

                    if (images != null) {
                        for (MultipartFile image : images) {
                            if (image.isEmpty()) continue;

                            String originalFilename = image.getOriginalFilename();

        // 공백은 언더스코어로, 나머지 특수문자 제거 (한글은 유지해도 무방하지만 확실히 하려면 아래처럼)
                            String sanitizedFilename = originalFilename
                                    .replaceAll("\\s+", "_")                      // 공백 -> 언더스코어
                                    .replaceAll("[^a-zA-Z0-9가-힣._-]", "_");      // 특수문자 -> 언더스코어

                            String uuid = UUID.randomUUID().toString();
                            String key = "image/" + uuid + "-" + sanitizedFilename;


                            ObjectMetadata metadata = new ObjectMetadata();
                            metadata.setContentLength(image.getSize());
                            metadata.setContentType(image.getContentType());

                            amazonS3.putObject(bucket, key, image.getInputStream(), metadata);
                            //amazonS3.setObjectAcl(bucket, key, CannedAccessControlList.PublicRead);



                            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
                            imageUrls.add(url);
                        }
                    }


                    dto.setImagePaths(imageUrls);
                    dto.setUser(user);

                    Portfolio savedPortfolio = portfolioService.savePortfolio(dto);
                    PortfolioResponseDto portfolioDto = convertToDtoForCreateOrUpdate(savedPortfolio);
                    UserSimpleDto userDto = new UserSimpleDto(user.getId(), user.getUsername());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(new PortfolioFullResponseDto(userDto, portfolioDto));


                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("🚩 실패: " + e.getMessage());
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
            public ResponseEntity<List<PortfolioFullResponseDto>> getMyPortfolios( @AuthenticationPrincipal UserDetails userDetails) {
                String loginId = userDetails.getUsername();
                User user = userService.findByLoginId(loginId);
                List<Portfolio> portfolios = portfolioService.getPortfoliosByUser(user);

                List<PortfolioFullResponseDto> responseDtos = portfolios.stream()
                        .map(p -> new PortfolioFullResponseDto(
                                new UserResponseDto(
                                        p.getUser().getId(),
                                        p.getUser().getLoginId(),
                                        p.getUser().getUsername()
                                ),
                                convertToDtoWithoutUser(p)
                        )).collect(Collectors.toList());


                return ResponseEntity.ok(responseDtos);
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
            public ResponseEntity<PortfolioFullResponseDto> getPortfolioById(@PathVariable Long id, HttpServletRequest request) {
                Optional<Portfolio> portfolioOpt = portfolioService.getPortfolioById(id);
                if (portfolioOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                Portfolio p = portfolioOpt.get();
                return ResponseEntity.ok(new PortfolioFullResponseDto(
                        new UserResponseDto(
                                p.getUser().getId(),
                                p.getUser().getLoginId(),
                                p.getUser().getUsername()
                        ),
                        convertToDtoWithoutUser(p)
                ));

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
                    @AuthenticationPrincipal UserDetails userDetails) {

                try {
                    String loginId = userDetails.getUsername();
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
                    description = "기존 포트폴리오 정보를 수정합니다. 이미지가 포함되지 않으면 기존 이미지 유지됩니다.",
                    responses = {
                            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = PortfolioFullResponseDto.class))),
                            @ApiResponse(responseCode = "404", description = "포트폴리오 없음"),
                            @ApiResponse(responseCode = "403", description = "권한 없음")
                    }
            )
            @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
            public ResponseEntity<?> updatePortfolio(
                    @Parameter(description = "포트폴리오 ID") @PathVariable Long id,
                    @AuthenticationPrincipal UserDetails userDetails,

                    @Parameter(
                            description = "수정할 포트폴리오 정보 (JSON)",
                            required = true,
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PortfolioDto.class)
                            )
                    )
                    @RequestPart("portfolio") PortfolioDto dto,

                    @Parameter(
                            description = "이미지 파일 (선택)",
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    )
                    @RequestPart(value = "images", required = false) MultipartFile[] images
            ) {
                // 기존 로직 유지

                try {
                    String loginId = userDetails.getUsername();
                    User user = userService.findByLoginId(loginId);

                    Optional<Portfolio> existingPortfolioOpt = portfolioService.getPortfolioById(id);
                    if (existingPortfolioOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("🚩 포트폴리오를 찾을 수 없습니다.");
                    }

                    Portfolio existingPortfolio = existingPortfolioOpt.get();

                    if (!existingPortfolio.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("🚫 수정 권한이 없습니다.");
                    }

                    List<String> imageUrls = new ArrayList<>();

                    if (images != null) {
                        for (MultipartFile file : images) {
                            if (file.isEmpty()) continue;

                            String originalFilename = file.getOriginalFilename();
                            String sanitizedFilename = originalFilename
                                    .replaceAll("\\s+", "_")
                                    .replaceAll("[^a-zA-Z0-9가-힣._-]", "_");

                            String uuid = UUID.randomUUID().toString();
                            String key = "image/" + uuid + "-" + sanitizedFilename;

                            ObjectMetadata metadata = new ObjectMetadata();
                            metadata.setContentLength(file.getSize());
                            metadata.setContentType(file.getContentType());

                            amazonS3.putObject(bucket, key, file.getInputStream(), metadata);

                            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
                            imageUrls.add(url);
                        }
                        dto.setImagePaths(imageUrls); // 새 이미지로 교체
                    } else {
                        dto.setImagePaths(existingPortfolio.getImagePaths()); // 이미지 유지
                    }

                    // 기존 포트폴리오에 dto 필드들을 조건적으로 덮어쓰기
                    if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
                        existingPortfolio.setTitle(dto.getTitle());
                    }
                    if (dto.getSubject() != null && !dto.getSubject().isBlank()) {
                        existingPortfolio.setSubject(dto.getSubject());
                    }
                    if (dto.getStartDate() != null && !dto.getStartDate().isBlank()) {
                        existingPortfolio.setStartDate(LocalDate.parse(dto.getStartDate()));
                    }
                    if (dto.getEndDate() != null && !dto.getEndDate().isBlank()) {
                        existingPortfolio.setEndDate(LocalDate.parse(dto.getEndDate()));
                    }
                    if (dto.getTeamSize() != null) {
                        existingPortfolio.setTeamSize(dto.getTeamSize());
                    }
                    if (dto.getSkills() != null && !dto.getSkills().isBlank()) {
                        existingPortfolio.setSkills(dto.getSkills());
                    }
                    if (dto.getRole() != null && !dto.getRole().isBlank()) {
                        existingPortfolio.setRole(dto.getRole());
                    }
                    if (dto.getDescriptions() != null &&
                            dto.getDescriptions().stream().anyMatch(s -> s != null && !s.isBlank())) {
                        existingPortfolio.setDescriptions(dto.getDescriptions());
                    }

                    if (dto.getImagePaths() != null) {
                        existingPortfolio.setImagePaths(dto.getImagePaths());
                    }

// 저장
                    Portfolio updatedPortfolio = portfolioService.updatePortfolio(existingPortfolio);


                    PortfolioResponseDto portfolioDto = convertToDtoForCreateOrUpdate(updatedPortfolio);
                    UserSimpleDto userDto = new UserSimpleDto(user.getId(), user.getUsername());
                    return ResponseEntity.ok(new PortfolioFullResponseDto(userDto, portfolioDto));

                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("🚩 수정 실패: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
            }

            private PortfolioResponseDto convertToDtoForCreateOrUpdate(Portfolio portfolio) {
                UserSimpleDto userDto = new UserSimpleDto(
                        portfolio.getUser().getId(),
                        portfolio.getUser().getUsername()
                );

                return new PortfolioResponseDto(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        portfolio.getSubject(),
                        portfolio.getStartDate().toString(),
                        portfolio.getEndDate().toString(),
                        portfolio.getTeamSize(),
                        portfolio.getSkills(),
                        portfolio.getRole(),
                        portfolio.getDescriptions(),
                        portfolio.getImagePaths()
                );
            }

            private PortfolioResponseDto convertToDtoForRead(Portfolio portfolio) {
                UserResponseDto userDto = new UserResponseDto(
                        portfolio.getUser().getId(),
                        portfolio.getUser().getLoginId(),
                        portfolio.getUser().getUsername()
                );

                return new PortfolioResponseDto(
                        portfolio.getId(),
                        portfolio.getTitle(),
                        portfolio.getSubject(),
                        portfolio.getStartDate().toString(),
                        portfolio.getEndDate().toString(),
                        portfolio.getTeamSize(),
                        portfolio.getSkills(),
                        portfolio.getRole(),
                        portfolio.getDescriptions(),
                        portfolio.getImagePaths()
                );
            }

            private PortfolioResponseDto convertToDtoWithoutUser(Portfolio p) {
                return new PortfolioResponseDto(
                        p.getId(),
                        p.getTitle(),
                        p.getSubject(),
                        p.getStartDate().toString(),
                        p.getEndDate().toString(),
                        p.getTeamSize(),
                        p.getSkills(),
                        p.getRole(),
                        p.getDescriptions(),
                        p.getImagePaths()
                );
            }




        }




        /*    @Operation(summary = "Presigned URL 요청", description = "S3에 업로드할 수 있는 presigned URL을 요청합니다.")
            @PostMapping("/preSignedUrl")
            public ResponseEntity<Map<String, String>> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
                Map<String, String> preSignedUrl = portfolioService.getPresignedUrl("image", request.getImageName());
                return ResponseEntity.ok(preSignedUrl)  ;
            }

        }*/