package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE1NjEyMzU4LCJleHBpcmVkX3N0YXR1cyI6IkZyb20iLCJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIn0.sdfsdgsdgsdgsdgsdgsd")
    private String accessToken;
}
