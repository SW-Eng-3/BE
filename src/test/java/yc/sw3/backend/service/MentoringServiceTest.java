package yc.sw3.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yc.sw3.backend.domain.mentoring.*;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.MentoringDto;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentoringServiceTest {

    @InjectMocks
    private MentoringService mentoringService;

    @Mock
    private MentoringRequestRepository mentoringRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private GamificationService gamificationService;

    private User mentee;
    private User mentor;

    @BeforeEach
    void setUp() {
        mentee = User.builder()
                .id(UUID.randomUUID())
                .email("mentee@yc.ac.kr")
                .name("Mentee")
                .build();
        
        mentor = User.builder()
                .id(UUID.randomUUID())
                .email("mentor@yc.ac.kr")
                .name("Mentor")
                .build();
    }

    @Test
    @DisplayName("멘토링 신청 성공")
    void applyMentoring_Success() {
        MentoringDto.ApplyRequest request = MentoringDto.ApplyRequest.builder()
                .mentorId(mentor.getId())
                .message("Help me please")
                .build();

        given(userRepository.findById(mentee.getId())).willReturn(Optional.of(mentee));
        given(userRepository.findById(mentor.getId())).willReturn(Optional.of(mentor));

        mentoringService.applyMentoring(mentee.getId(), request);

        verify(mentoringRequestRepository, times(1)).save(any(MentoringRequest.class));
        verify(emailService, times(1)).sendNotification(eq(mentor.getEmail()), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("멘토링 상태 변경 및 포인트 지급")
    void updateStatus_Completed_Success() {
        UUID requestId = UUID.randomUUID();
        MentoringRequest mentoringRequest = MentoringRequest.builder()
                .id(requestId)
                .mentee(mentee)
                .mentor(mentor)
                .status(MentoringStatus.REQUESTED)
                .build();

        given(mentoringRequestRepository.findById(requestId)).willReturn(Optional.of(mentoringRequest));

        mentoringService.updateStatus(mentor.getId(), requestId, MentoringStatus.COMPLETED);

        assertThat(mentoringRequest.getStatus()).isEqualTo(MentoringStatus.COMPLETED);
        verify(gamificationService, times(1)).awardPoints(eq(mentor.getId()), eq(50), any());
        verify(gamificationService, times(1)).awardPoints(eq(mentee.getId()), eq(10), any());
    }

    @Test
    @DisplayName("멘토에게 온 신청 목록 조회")
    void getMentorRequests_Success() {
        MentoringRequest request = MentoringRequest.builder()
                .id(UUID.randomUUID())
                .mentee(mentee)
                .mentor(mentor)
                .message("Test message")
                .status(MentoringStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(mentor.getId())).willReturn(Optional.of(mentor));
        given(mentoringRequestRepository.findAllByMentor(mentor)).willReturn(java.util.List.of(request));

        java.util.List<MentoringDto.Response> responses = mentoringService.getMentorRequests(mentor.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMenteeName()).isEqualTo(mentee.getName());
        assertThat(responses.get(0).getMessage()).isEqualTo("Test message");
    }
}
