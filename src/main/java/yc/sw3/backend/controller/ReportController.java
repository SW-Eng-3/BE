package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.dto.ReportDto;
import yc.sw3.backend.service.ReportService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 접수", description = "게시글, 댓글 또는 사용자를 신고합니다.")
    @PostMapping
    public ResponseEntity<UUID> createReport(
            @AuthenticationPrincipal String userId,
            @RequestBody ReportDto.CreateRequest request) {
        return ResponseEntity.ok(reportService.createReport(UUID.fromString(userId), request));
    }

    @Operation(summary = "신고 내역 조회 (관리자)", description = "모든 신고 내역을 최신순으로 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDto.Response>> getReports() {
        return ResponseEntity.ok(reportService.getReports());
    }

    @Operation(summary = "신고 처리 (관리자)", description = "신고 상태를 변경합니다 (처리됨, 기각됨 등).")
    @PatchMapping("/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> processReport(
            @PathVariable UUID reportId,
            @RequestBody ReportDto.ProcessRequest request) {
        reportService.processReport(reportId, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
