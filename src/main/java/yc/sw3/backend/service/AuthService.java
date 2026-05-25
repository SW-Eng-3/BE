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
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String AUTH_CODE_PREFIX = "AUTH_CODE:";
    private static final long AUTH_CODE_EXPIRATION_MINUTES = 5;

    @Transactional
    public void sendCode(String email) {
        if (!email.endsWith("@yc.ac.kr") && !email.equals("rla005@naver.com")) {
            throw new IllegalArgumentException("허용되지 않은 이메일 주소입니다.");
        }
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);
        
        redisTemplate.opsForValue().set(
                AUTH_CODE_PREFIX + email,
                code,
                java.time.Duration.ofMinutes(AUTH_CODE_EXPIRATION_MINUTES)
        );

        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(AUTH_CODE_PREFIX + email);
        
        if (savedCode == null) {
            return false;
        }

        boolean isValid = savedCode.equals(code);
        if (isValid) {
            redisTemplate.delete(AUTH_CODE_PREFIX + email);
            userRepository.findByEmail(email).ifPresent(User::verify);
        }
        return isValid;
    }

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (!request.getEmail().endsWith("@yc.ac.kr") && !request.getEmail().equals("rla005@naver.com")) {
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
