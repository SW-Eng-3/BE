package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.config.JwtTokenProvider;
import yc.sw3.backend.domain.user.*;
import yc.sw3.backend.dto.AuthDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private static class VerificationInfo {
        String code;
        long createdAt;

        VerificationInfo(String code) {
            this.code = code;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 5 * 60 * 1000; // 5분
        }
    }

    private final java.util.Map<String, VerificationInfo> verificationCodes = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public void sendCode(String email) {
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);
        verificationCodes.put(email, new VerificationInfo(code));
        emailService.sendVerificationCode(email, code);
    }

    public boolean verifyCode(String email, String code) {
        VerificationInfo info = verificationCodes.get(email);
        if (info == null || info.isExpired()) {
            verificationCodes.remove(email);
            return false;
        }
        boolean isValid = info.code.equals(code);
        if (isValid) {
            verificationCodes.remove(email);
        }
        return isValid;
    }

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .user(savedUser)
                .build();

        profileRepository.save(profile);
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        return AuthDto.TokenResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .build();
    }

    public AuthDto.ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        return AuthDto.ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .major(profile.getMajor())
                .currentCompany(profile.getCurrentCompany())
                .jobCategory(profile.getJobCategory())
                .country(profile.getCountry())
                .bio(profile.getBio())
                .build();
    }

    @Transactional
    public void verifyUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.verify();
    }
}
