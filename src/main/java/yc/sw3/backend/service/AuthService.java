package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.config.security.JwtTokenProvider;
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

    // 인메모리 인증번호 저장소 및 만료 시간 관리용 내부 클래스
    private static class VerificationInfo {
        private final String code;
        private final java.time.LocalDateTime expiresAt;

        public VerificationInfo(String code, long expirationMinutes) {
            this.code = code;
            this.expiresAt = java.time.LocalDateTime.now().plusMinutes(expirationMinutes);
        }

        public boolean isExpired() {
            return java.time.LocalDateTime.now().isAfter(expiresAt);
        }
    }

    private final java.util.Map<String, VerificationInfo> verificationStore = new java.util.concurrent.ConcurrentHashMap<>();

    private static final long AUTH_CODE_EXPIRATION_MINUTES = 5;

    @Transactional
    public void sendCode(String email) {
        if (!email.endsWith("@yc.ac.kr") && !email.equals("rla030526@gmail.com")) {
            throw new IllegalArgumentException("허용되지 않은 이메일 주소입니다.");
        }
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);
        
        verificationStore.put(email, new VerificationInfo(code, AUTH_CODE_EXPIRATION_MINUTES));

        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        VerificationInfo info = verificationStore.get(email);
        
        if (info == null || info.isExpired()) {
            verificationStore.remove(email);
            return false;
        }

        boolean isValid = info.code.equals(code);
        if (isValid) {
            verificationStore.remove(email);
            userRepository.findByEmail(email).ifPresent(User::verify);
        }
        return isValid;
    }

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (!request.getEmail().endsWith("@yc.ac.kr") && !request.getEmail().equals("rla030526@gmail.com")) {
            throw new IllegalArgumentException("허용되지 않은 이메일 주소입니다.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
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
                .points(0)
                .build();

        profileRepository.save(profile);
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());

        return AuthDto.TokenResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .build();
    }

    public AuthDto.TokenResponse createTestToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());

        return AuthDto.TokenResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .build();
    }

    public AuthDto.ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        return AuthDto.ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .major(profile.getMajor())
                .majorDescription(profile.getMajor() != null ? profile.getMajor().getDescription() : null)
                .currentCompany(profile.getCurrentCompany())
                .jobCategory(profile.getJobCategory())
                .jobCategoryDescription(profile.getJobCategory() != null ? profile.getJobCategory().getDescription() : null)
                .country(profile.getCountry())
                .countryDescription(profile.getCountry() != null ? profile.getCountry().getDescription() : null)
                .bio(profile.getBio())
                .points(profile.getPoints())
                .build();
    }

    @Transactional
    public void updateProfile(UUID userId, AuthDto.ProfileUpdateRequest request) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));

        profile.update(
                request.getMajor(),
                request.getCurrentCompany(),
                request.getJobCategory(),
                request.getCountry(),
                request.getBio()
        );
    }

    @Transactional
    public void verifyUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.verify();
    }
}
