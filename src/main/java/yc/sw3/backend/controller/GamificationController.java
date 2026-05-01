package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.domain.gamification.PointHistoryRepository;
import yc.sw3.backend.domain.user.Profile;
import yc.sw3.backend.domain.user.ProfileRepository;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.dto.PointDto;
import yc.sw3.backend.service.GamificationService;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Gamification", description = "포인트 및 게이미피케이션 API")
@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final ProfileRepository profileRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Operation(summary = "내 현재 포인트 조회")
    @GetMapping("/points")
    public ResponseEntity<PointDto.SummaryResponse> getMyPoints(@AuthenticationPrincipal UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        
        return ResponseEntity.ok(PointDto.SummaryResponse.builder()
                .totalPoints(profile.getPoints())
                .build());
    }

    @Operation(summary = "내 포인트 히스토리 조회")
    @GetMapping("/history")
    public ResponseEntity<List<PointDto.HistoryResponse>> getMyHistory(@AuthenticationPrincipal UUID userId) {
        List<PointDto.HistoryResponse> history = pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(h -> PointDto.HistoryResponse.builder()
                        .id(h.getId())
                        .amount(h.getAmount())
                        .reason(h.getReason())
                        .reasonDescription(h.getReason().getDescription())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "테스트용 포인트 지급 (관리자/테스트용)")
    @PostMapping("/test/award")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> awardTestPoints(
            @AuthenticationPrincipal UUID userId,
            @RequestBody PointDto.TestAwardRequest request) {
        
        gamificationService.awardPoints(userId, request.getAmount(), request.getReason());
        return ResponseEntity.ok("포인트가 지급되었습니다.");
    }

    @Operation(summary = "테스트용 포인트 차감 (관리자/테스트용)")
    @PostMapping("/test/deduct")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deductTestPoints(
            @AuthenticationPrincipal UUID userId,
            @RequestBody PointDto.TestAwardRequest request) {
        
        gamificationService.deductPoints(userId, request.getAmount(), request.getReason());
        return ResponseEntity.ok("포인트가 차감되었습니다.");
    }
}
