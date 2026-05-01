package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.community.PostRepository;
import yc.sw3.backend.domain.community.CommentRepository;
import yc.sw3.backend.domain.report.Report;
import yc.sw3.backend.domain.report.ReportRepository;
import yc.sw3.backend.domain.report.ReportStatus;
import yc.sw3.backend.domain.report.ReportTargetType;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.ReportDto;
import yc.sw3.backend.domain.gamification.PointReason;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public UUID createReport(UUID reporterId, ReportDto.CreateRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .description(request.getDescription())
                .build();

        return reportRepository.save(report).getId();
    }

    public List<ReportDto.Response> getReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processReport(UUID reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        report.process(status);

        if (status == ReportStatus.PROCESSED) {
            applySanctions(report);
        }
    }

    private void applySanctions(Report report) {
        UUID targetUserId = null;
        
        // 신고 대상 타입에 따라 유저 ID 추출
        if (report.getTargetType() == ReportTargetType.USER) {
            targetUserId = report.getTargetId();
        } else if (report.getTargetType() == ReportTargetType.POST) {
            targetUserId = postRepository.findById(report.getTargetId())
                    .map(post -> post.getAuthor().getId())
                    .orElse(null);
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            targetUserId = commentRepository.findById(report.getTargetId())
                    .map(comment -> comment.getUser().getId())
                    .orElse(null);
        }

        if (targetUserId == null) return;

        User targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser == null) return;

        // 유저 전체에 대한 누적 처리된 신고 횟수 조회
        long totalUserReports = reportRepository.countByTargetUserIdAndStatus(targetUserId, ReportStatus.PROCESSED);

        // 자동 제재 정책 적용
        if (totalUserReports == 1) {
            System.out.println("경고: 유저 " + targetUser.getName() + "님에 대한 첫 신고가 처리되었습니다.");
        } else if (totalUserReports == 3) {
            gamificationService.deductPoints(targetUserId, 100, PointReason.ADMIN_ADJUSTMENT);
        } else if (totalUserReports == 5) {
            targetUser.restrict(7); // 7일 제한
        } else if (totalUserReports >= 10) {
            System.out.println("CRITICAL: 유저 " + targetUser.getName() + "님이 영구 정지 검토 대상입니다.");
        }
    }

    private ReportDto.Response convertToDto(Report report) {
        return ReportDto.Response.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getName())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .reasonDescription(report.getReason().getDescription())
                .description(report.getDescription())
                .status(report.getStatus())
                .statusDescription(report.getStatus().getDescription())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
