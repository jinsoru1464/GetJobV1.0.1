package com.example.GetJobV101.controller;

import com.example.GetJobV101.dto.JoinRequest;
import com.example.GetJobV101.dto.JwtResponse;
import com.example.GetJobV101.dto.LoginRequest;
import com.example.GetJobV101.dto.LoginResponse;
import com.example.GetJobV101.jwt.JwtTokenProvider;
import com.example.GetJobV101.repository.UserRepository;
import com.example.GetJobV101.service.UserService;
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
public class AuthController {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;  // JwtTokenProvider 추가

    // 생성자에 JwtTokenProvider 주입
    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;  // jwtTokenProvider 주입

    }
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
            summary = "임시 엑세스 토큰 발급",
            description = "임시 사용자(`temporary`)에 대해 JWT 토큰을 발급합니다. 이 토큰은 테스트 용도로 사용되며, 역할은 `USER`로 설정됩니다. " +
                    "발급된 토큰은 `Authorization` 헤더에 포함하여 테스트할 API에 접근할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "임시 엑세스 토큰 발급 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))
                    )
            }
    )
    @GetMapping("/temporary-token")
    public ResponseEntity<Map<String, String>> generateTemporaryToken() {
        System.out.println("💡 컨트롤러 도착: /api/auth/temporary-token");
        String token = userService.generateTemporaryToken();

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);

        return ResponseEntity.ok(response);
    }



}