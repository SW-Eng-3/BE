package yc.sw3.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import yc.sw3.backend.config.JwtTokenProvider;
import yc.sw3.backend.domain.user.ProfileRepository;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.AuthDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private EmailService emailService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .name("Test User")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        AuthDto.SignupRequest request = AuthDto.SignupRequest.builder()
                .email("new@example.com")
                .password("password")
                .name("New User")
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encoded");
        given(userRepository.save(any())).willReturn(user);

        authService.signup(request);

        verify(userRepository, times(1)).save(any());
        verify(profileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void signup_Fail_DuplicateEmail() {
        AuthDto.SignupRequest request = AuthDto.SignupRequest.builder()
                .email("test@example.com")
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), anyString())).willReturn("jwt-token");

        AuthDto.TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공")
    void verifyCode_Success() {
        String email = "test@example.com";
        String code = "123456";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("AUTH_CODE:" + email)).willReturn(code);

        boolean result = authService.verifyCode(email, code);

        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).delete("AUTH_CODE:" + email);
    }
}
