package com.online_library_service.Service;

import com.online_library_service.dto.*;
import com.online_library_service.entity.User;
import com.online_library_service.enums.Role;
import com.online_library_service.repository.UserRepository;
import com.online_library_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    public AuthenticationResponse register(RegisterRequest request) {
        log.debug("Registering user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);
        user.setMembershipStartDate(LocalDate.now());
        user.setMembershipEndDate(LocalDate.now().plusMonths(request.getMembershipMonths()));

        User savedUser = userRepository.save(user);

        try {
            String subject = "Welcome to Online Library";
            String body = String.format(
                    "Hi %s,\n\nWelcome to the library! Your membership is valid until %s.\n\nEnjoy reading!\n\nRegards,\nLibrary Team",
                    savedUser.getName(),
                    savedUser.getMembershipEndDate()
            );
            notificationService.sendEmail(savedUser.getEmail(), subject, body);
            log.info("Welcome email sent to {}", savedUser.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}", savedUser.getEmail(), ex);
        }

        String jwtToken = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtUtil.extractUsername(request.getRefreshToken());
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthenticationResponse(newToken);
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = jwtUtil.generateResetToken(user.getEmail());

        log.info("Password reset token generated for user: {}", user.getEmail());
        return "Password reset link sent to your email";
    }

    public String resetPassword(ResetPasswordRequest request) {
        String email = jwtUtil.extractUsername(request.getToken());
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password reset successfully";
    }

    public String changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }

    public String logout(String token) {
        log.info("User logged out with token: {}", token);
        return "Logged out successfully";
    }

    public UserDto getCurrentUser(String token) {
        String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getMembershipStartDate(),
                user.getMembershipEndDate()
        );
    }
}
