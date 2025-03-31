package com.example.GetJobV101.controller;

import com.example.GetJobV101.dto.JoinRequest;
import com.example.GetJobV101.dto.LoginRequest;
import com.example.GetJobV101.dto.LoginResponse;
import com.example.GetJobV101.service.UserService;
import com.example.GetJobV101.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "회원가입",
            description = "이메일과 비밀번호를 이용하여 새 사용자를 등록합니다. 비밀번호는 암호화하여 저장됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "이미 사용 중인 로그인 ID 또는 비밀번호 확인 실패", content = @Content(mediaType = "application/json"))
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid JoinRequest request) {
        String result = userService.signup(request);
        if (result.equals("회원가입 성공!")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Operation(
            summary = "로그인",
            description = "사용자의 로그인 정보를 기반으로 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "400", description = "이메일 또는 비밀번호가 올바르지 않음", content = @Content(mediaType = "application/json"))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        if (response == null) {
            return ResponseEntity.badRequest().body("🚫 이메일 또는 비밀번호가 올바르지 않습니다.");
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "사용자가 입력한 이메일이 이미 사용 중인지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이메일 사용 가능 여부", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
            }
    )
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(exists);  // true면 이미 존재하는 이메일
    }

    @Operation(
            summary = "임시 JWT 토큰 발급",
            description = "비회원 또는 테스트 용도로 사용할 수 있는 임시 JWT 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "임시 토큰 반환", content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping("/temporary-token")
    public ResponseEntity<Map<String, String>> getTemporaryToken() {
        String token = jwtUtil.generateTemporaryToken();
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        return ResponseEntity.ok(response);
    }
}
