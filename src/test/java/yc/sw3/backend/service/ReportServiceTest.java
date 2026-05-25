package yc.sw3.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yc.sw3.backend.domain.report.*;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.ReportDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GamificationService gamificationService;

    private User reporter;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .id(UUID.randomUUID())
                .email("reporter@yc.ac.kr")
                .name("Reporter")
                .build();
    }

    @Test
    @DisplayName("신고 생성 성공")
    void createReport_Success() {
        ReportDto.CreateRequest request = ReportDto.CreateRequest.builder()
                .targetType(ReportTargetType.POST)
                .targetId(UUID.randomUUID())
                .reason(ReportReason.INAPPROPRIATE_CONTENT)
                .description("Bad post")
                .build();

        given(userRepository.findById(reporter.getId())).willReturn(Optional.of(reporter));
        
        Report savedReport = Report.builder()
                .id(UUID.randomUUID())
                .reporter(reporter)
                .build();
        given(reportRepository.save(any(Report.class))).willReturn(savedReport);

        UUID reportId = reportService.createReport(reporter.getId(), request);

        assertThat(reportId).isEqualTo(savedReport.getId());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 처리 - 포인트 차감 정책 확인")
    void processReport_WithDeductPoints() {
        UUID reportId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        User targetUser = User.builder().id(targetUserId).name("Bad User").build();

        Report report = Report.builder()
                .id(reportId)
                .targetType(ReportTargetType.USER)
                .targetId(targetUserId)
                .status(ReportStatus.PENDING)
                .build();

        given(reportRepository.findById(reportId)).willReturn(Optional.of(report));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(reportRepository.countByTargetUserIdAndStatus(targetUserId, ReportStatus.PROCESSED)).willReturn(3L);

        reportService.processReport(reportId, ReportStatus.PROCESSED);

        verify(gamificationService, times(1)).deductPoints(eq(targetUserId), eq(100), any());
    }
}
