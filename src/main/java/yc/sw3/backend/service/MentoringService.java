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

    @Transactional
    public void applyMentoring(UUID menteeId, MentoringDto.ApplyRequest request) {
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
    public void updateStatus(UUID requestId, MentoringStatus status) {
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        mentoringRequest.updateStatus(status);
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
}
