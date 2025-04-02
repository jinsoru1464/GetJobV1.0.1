package com.example.GetJobV101.service;

import com.example.GetJobV101.dto.JoinRequest;
import com.example.GetJobV101.dto.LoginRequest;
import com.example.GetJobV101.dto.LoginResponse;
import com.example.GetJobV101.entity.User;
import com.example.GetJobV101.jwt.JwtTokenProvider;
import com.example.GetJobV101.jwt.JwtUtil;
import com.example.GetJobV101.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider만 사용

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) { // 생성자 주입
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String signup(JoinRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            return "이미 사용 중인 로그인 ID입니다.";
        }

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            return "비밀번호와 확인이 일치하지 않습니다.";
        }

        // 비밀번호 암호화 후 저장
        User user = request.toEntity(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return "회원가입 성공!";
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String role = "ROLE_USER"; // 기본적으로 ROLE_USER 부여
        String token = jwtTokenProvider.generateToken(user.getLoginId(), role); // 기존 JWT 생성
        return new LoginResponse(token);
    }

    public String generateTemporaryToken() {
        User user = userRepository.findByLoginId("temporary")
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .loginId("temporary")
                            .username("임시 유저")
                            .password(passwordEncoder.encode("temp1234"))
                            .role("ROLE_USER")
                            .build();

                    return userRepository.save(newUser);
                });

        return jwtTokenProvider.generateToken(user.getLoginId(), user.getRole());
    }



    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByLoginId(email);
    }


}


