package com.example.GetJobV101.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 정보")
@Entity
@Table(name = "users") // 'user'는 예약어라서 안전하게 'users'로 설정
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일 형식의 로그인 아이디", example = "user@example.com")
    private String loginId;

    @Schema(description = "사용자의 이름", example = "홍길동")
    private String username;

    @Schema(description = "사용자의 비밀번호")
    private String password;
}
