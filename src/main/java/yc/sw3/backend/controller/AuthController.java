package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.dto.AuthDto;
import yc.sw3.backend.service.AuthService;

import java.util.UUID;

@Tag(name = "Auth & User", description = "인증 및 사용자 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "역할(Role)별 회원 정보를 저장합니다. 비밀번호는 BCrypt로 암호화됩니다.")
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/auth/login")
    public ResponseEntity<AuthDto.TokenResponse> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "프로필 조회", description = "사용자의 상세 프로필 정보를 조회합니다.")
    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<AuthDto.ProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @Operation(summary = "프로필 수정", description = "자신의 프로필 정보를 수정합니다.")
    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<Void> updateProfile(@PathVariable UUID userId, @RequestBody AuthDto.ProfileUpdateRequest request) {
        authService.updateProfile(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가입 승인", description = "관리자가 증빙 서류 검토 후 가입을 승인합니다.")
    @PatchMapping("/admin/users/{userId}/status")
    public ResponseEntity<Void> approveUser(@PathVariable UUID userId) {
        authService.verifyUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증 발송", description = "입력한 이메일로 6자리 인증 코드를 발송합니다.")
    @PostMapping("/auth/email/send")
    public ResponseEntity<Void> sendEmail(@RequestParam String email) {
        authService.sendCode(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 코드 검증", description = "입력한 인증 코드가 일치하는지 확인합니다.")
    @PostMapping("/auth/email/verify")
    public ResponseEntity<Boolean> verifyEmail(@RequestBody AuthDto.VerificationRequest request) {
        return ResponseEntity.ok(authService.verifyCode(request.getEmail(), request.getCode()));
    }
}
