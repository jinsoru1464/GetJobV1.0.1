package com.example.GetJobV101.dto;

import com.example.GetJobV101.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class JoinRequest {

    @NotBlank(message = "로그인 아이디가 비어있습니다.")
    @Schema(description = "회원가입 시 사용할 로그인 아이디 (이메일 형식)", example = "user@example.com")
    private String loginId;

    @NotBlank(message = "비밀번호가 비어있습니다.")
    @Schema(description = "사용자의 비밀번호", example = "password123")
    private String password;

    @Schema(description = "비밀번호 확인", example = "password123")
    private String passwordCheck;

    @NotBlank(message = "이름이 비어있습니다.")
    @Schema(description = "사용자의 이름", example = "홍길동")
    private String username;


    // 비밀번호 암호화
    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(this.loginId)
                .password(encodedPassword)
                .username(this.username)
                .build();
    }
}
