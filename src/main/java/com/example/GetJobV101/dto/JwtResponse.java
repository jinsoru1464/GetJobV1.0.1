package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    @Schema(description = "JWT 임시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE1NjEyMzU4LCJleHBpcmVkX3N0YXR1cyI6IkZyb20iLCJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIn0.sdfsdgsdgsdgsdgsdgsd")
    private String token;

}

