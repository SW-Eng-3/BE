package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.mentoring.*;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.MentoringDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringService {

    private final MentoringRequestRepository mentoringRequestRepository;
    private final MentorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Transactional
    public void applyMentoring(UUID menteeId, MentoringDto.ApplyRequest request) {
        // ... (existing code)
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        User mentor = userRepository.findById(request.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        MentoringRequest mentoringRequest = MentoringRequest.builder()
                .mentee(mentee)
                .mentor(mentor)
                .message(request.getMessage())
                .status(MentoringStatus.REQUESTED)
                .build();

        mentoringRequestRepository.save(mentoringRequest);
    }

    public List<MentoringDto.Response> getMyRequests(UUID menteeId) {
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        return mentoringRequestRepository.findAllByMentee(mentee).stream()
                .map(m -> MentoringDto.Response.builder()
                        .id(m.getId())
                        .menteeName(m.getMentee().getName())
                        .mentorName(m.getMentor().getName())
                        .message(m.getMessage())
                        .status(m.getStatus())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelMentoring(UUID userId, UUID requestId) {
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!mentoringRequest.getMentee().getId().equals(userId)) {
            throw new IllegalStateException("본인이 신청한 멘토링만 취소할 수 있습니다.");
        }
        
        mentoringRequest.updateStatus(MentoringStatus.CANCELLED);
    }

    @Transactional
    public void updateStatus(UUID userId, UUID requestId, MentoringStatus status) {
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // 멘토 본인만 수락/거절/완료 가능
        if (!mentoringRequest.getMentor().getId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        mentoringRequest.updateStatus(status);

        // 멘토링 완료 시 포인트 지급
        if (status == MentoringStatus.COMPLETED) {
            // 멘토에게 50점 지급
            gamificationService.awardPoints(mentoringRequest.getMentor().getId(), 50, yc.sw3.backend.domain.gamification.PointReason.MENTORING_COMPLETED);
            // 멘티에게 10점 지급
            gamificationService.awardPoints(mentoringRequest.getMentee().getId(), 10, yc.sw3.backend.domain.gamification.PointReason.MENTORING_COMPLETED);
        }
    }

    @Transactional
    public void registerSchedule(UUID mentorId, MentoringDto.ScheduleRequest request) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        MentorSchedule schedule = MentorSchedule.builder()
                .mentor(mentor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        scheduleRepository.save(schedule);
    }

    public List<MentoringDto.ScheduleResponse> getMentorSchedules(UUID mentorId) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        return scheduleRepository.findAllByMentor(mentor).stream()
                .map(s -> MentoringDto.ScheduleResponse.builder()
                        .id(s.getId())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }
}
