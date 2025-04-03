package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "포트폴리오 등록/수정 multipart 요청")
public class PortfolioUploadRequest {

    @Schema(description = "포트폴리오 JSON 본문", type = "string", format = "binary", required = true)
    private String portfolio;  // 실제로는 JSON 문자열로 들어감

    @Schema(description = "업로드할 이미지들 (선택)", type = "string", format = "binary")
    private List<MultipartFile> images;
}
