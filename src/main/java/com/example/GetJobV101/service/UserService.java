package com.example.GetJobV101.service;

import com.example.GetJobV101.dto.JoinRequest;
import com.example.GetJobV101.dto.LoginRequest;
import com.example.GetJobV101.dto.LoginResponse;
import com.example.GetJobV101.entity.User;
import com.example.GetJobV101.jwt.JwtUtil;
import com.example.GetJobV101.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // ✅ 추가

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) { // ✅ 생성자 주입
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getLoginId());
        return new LoginResponse(token);
    }

    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByLoginId(email);
    }

}

