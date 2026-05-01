package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.domain.mentoring.MentoringStatus;
import yc.sw3.backend.dto.MentoringDto;
import yc.sw3.backend.service.MentoringService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Mentoring", description = "멘토링 및 면담 시스템 API")
@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;

    @Operation(summary = "오피스 아워 설정", description = "상담 가능 시간대를 등록합니다.")
    @PostMapping("/schedule")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MENTOR') or hasRole('GRADUATE') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> registerSchedule(@AuthenticationPrincipal UUID userId, @RequestBody MentoringDto.ScheduleRequest request) {
        mentoringService.registerSchedule(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "면담 신청", description = "멘토에게 멘토링/커피챗을 신청합니다.")
    @PostMapping("/apply")
    public ResponseEntity<Void> applyMentoring(@AuthenticationPrincipal UUID userId, @RequestBody MentoringDto.ApplyRequest request) {
        mentoringService.applyMentoring(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 신청 현황 조회")
    @GetMapping("/my-requests")
    public ResponseEntity<List<MentoringDto.Response>> getMyRequests(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(mentoringService.getMyRequests(userId));
    }

    @Operation(summary = "면담 신청 취소", description = "멘티가 신청한 멘토링을 취소합니다.")
    @DeleteMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelMentoring(@AuthenticationPrincipal UUID userId, @PathVariable UUID requestId) {
        mentoringService.cancelMentoring(userId, requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "면담 상태 변경", description = "멘토가 신청을 수락/거절하거나 완료 처리합니다.")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID requestId,
            @RequestParam MentoringStatus status) {
        mentoringService.updateStatus(userId, requestId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "멘토 일정 조회", description = "특정 멘토의 상담 가능 시간대를 조회합니다.")
    @GetMapping("/mentors/{mentorId}/schedules")
    public ResponseEntity<List<MentoringDto.ScheduleResponse>> getMentorSchedules(@PathVariable UUID mentorId) {
        return ResponseEntity.ok(mentoringService.getMentorSchedules(mentorId));
    }
}
