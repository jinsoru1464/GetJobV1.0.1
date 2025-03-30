package com.example.GetJobV101.service;

import com.example.GetJobV101.entity.User;
import com.example.GetJobV101.repository.UserRepository;
import com.example.GetJobV101.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginId));
        return new UserPrincipal(user);
    }
}
