package com.analyfy.analify.Service;

import com.analyfy.analify.Entity.*;
import com.analyfy.analify.Repository.UserRepository;
import com.analyfy.analify.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Map<String, Object> authenticate(String email, String password) {
        // 1. Find User
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials: User not found"));

        // 2. Validate Password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials: Bad password");
        }

        // 3. Determine Role Dynamically (Fixes getRole error)
        String role = determineRole(user);
        Long userId = user.getUserId();

        // 4. Generate Token
        String token = jwtTokenProvider.generateToken(userId, role, email);

        // 5. Build Response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", userId);
        response.put("role", role);

        return response;
    }

    private String determineRole(User user) {
        if (user instanceof AdminG) return "ADMIN_G";
        if (user instanceof AdminStore) return "ADMIN_STORE";
        if (user instanceof Caissier) return "CAISSIER";
        if (user instanceof Investor) return "INVESTOR";
        return "USER"; // Fallback
    }
}   