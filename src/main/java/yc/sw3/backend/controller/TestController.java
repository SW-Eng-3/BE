package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.dto.AuthDto;
import yc.sw3.backend.service.AuthService;
import yc.sw3.backend.service.EmailService;

@Tag(name = "Test", description = "개발 및 테스트용 API")
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailService emailService;
    private final AuthService authService;

    @Operation(summary = "메일 발송 테스트", description = "입력한 이메일로 테스트 메일을 즉시 발송합니다.")
    @PostMapping("/email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        emailService.sendNotification(
            to,
            "테스트 메일입니다.",
            "이메일 발송 테스트",
            "이메일 발송 테스트가 성공했습니다!"
        );
        return ResponseEntity.ok("테스트 메일이 발송되었습니다. [" + to + "]를 확인하세요.");
    }

    @Operation(summary = "임시 토큰 발급", description = "비밀번호 검증 없이 해당 이메일의 사용자로 로그인 토큰을 발급합니다. (개발용)")
    @PostMapping("/token")
    public ResponseEntity<AuthDto.TokenResponse> getTestToken(@RequestParam String email) {
        return ResponseEntity.ok(authService.createTestToken(email));
    }
}
