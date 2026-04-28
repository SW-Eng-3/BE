package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.domain.mentoring.MentoringStatus;
import yc.sw3.backend.dto.MentoringDto;
import yc.sw3.backend.service.MentoringService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Mentoring", description = "멘토링 및 면담 시스템 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;

    @Operation(summary = "오피스 아워 설정", description = "상담 가능 시간대를 등록합니다.")
    @PostMapping("/mentors/schedule")
    public ResponseEntity<Void> registerSchedule(@RequestHeader("X-User-Id") UUID mentorId, @RequestBody MentoringDto.ScheduleRequest request) {
        mentoringService.registerSchedule(mentorId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "면담 신청", description = "멘토에게 멘토링/커피챗을 신청합니다.")
    @PostMapping("/mentoring/apply")
    public ResponseEntity<Void> applyMentoring(@RequestHeader("X-User-Id") UUID menteeId, @RequestBody MentoringDto.ApplyRequest request) {
        mentoringService.applyMentoring(menteeId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "신청 현황 조회")
    @GetMapping("/mentoring/my-requests")
    public ResponseEntity<List<MentoringDto.Response>> getMyRequests(@RequestHeader("X-User-Id") UUID menteeId) {
        return ResponseEntity.ok(mentoringService.getMyRequests(menteeId));
    }

    @Operation(summary = "면담 상태 변경")
    @PatchMapping("/mentoring/{requestId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID requestId, @RequestParam MentoringStatus status) {
        mentoringService.updateStatus(requestId, status);
        return ResponseEntity.ok().build();
    }
}
