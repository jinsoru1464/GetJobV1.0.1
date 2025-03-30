package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    @NotBlank
    @Schema(description = "로그인에 사용할 아이디 (이메일 형식)", example = "user@example.com")
    private String loginId;

    @NotBlank
    @Schema(description = "사용자 비밀번호", example = "password123")
    private String password;
}
