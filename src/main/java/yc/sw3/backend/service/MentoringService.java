package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.mentoring.*;
import yc.sw3.backend.domain.user.ProfileRepository;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.MentoringDto;

import java.time.LocalDateTime;
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
    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    public List<yc.sw3.backend.dto.AuthDto.ProfileResponse> getMentorList(
            yc.sw3.backend.domain.user.Major major,
            yc.sw3.backend.domain.user.JobCategory jobCategory,
            yc.sw3.backend.domain.user.Country country,
            String name) {
        
        List<yc.sw3.backend.domain.user.Role> mentorRoles = List.of(
                yc.sw3.backend.domain.user.Role.GRADUATE,
                yc.sw3.backend.domain.user.Role.PROFESSOR
        );

        return userRepository.findByRoleIn(mentorRoles).stream()
                .map(user -> {
                    yc.sw3.backend.domain.user.Profile profile = profileRepository.findById(user.getId()).orElse(null);
                    return yc.sw3.backend.dto.AuthDto.ProfileResponse.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .major(profile != null ? profile.getMajor() : null)
                            .majorDescription(profile != null && profile.getMajor() != null ? profile.getMajor().getDescription() : null)
                            .currentCompany(profile != null ? profile.getCurrentCompany() : null)
                            .jobCategory(profile != null ? profile.getJobCategory() : null)
                            .country(profile != null ? profile.getCountry() : null)
                            .bio(profile != null ? profile.getBio() : null)
                            .build();
                })
                .filter(p -> major == null || p.getMajor() == major)
                .filter(p -> jobCategory == null || p.getJobCategory() == jobCategory)
                .filter(p -> country == null || p.getCountry() == country)
                .filter(p -> name == null || p.getName().contains(name))
                .collect(Collectors.toList());
    }

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

        // 멘토에게 알림 메일 발송
        emailService.sendNotification(
            mentor.getEmail(),
            "새로운 멘토링 신청이 도착했습니다.",
            "멘토링 신청 알림",
            mentee.getName() + "님으로부터 새로운 멘토링 신청이 도착했습니다.\n내용: " + request.getMessage()
        );
    }

    public List<MentoringDto.Response> getMyRequests(UUID menteeId) {
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        return mentoringRequestRepository.findAllByMentee(mentee).stream()
                .map(m -> MentoringDto.Response.builder()
                        .id(m.getId())
                        .mentorId(m.getMentor().getId())
                        .mentorName(m.getMentor().getName())
                        .menteeId(m.getMentee().getId())
                        .menteeName(m.getMentee().getName())
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
        
        if (!mentoringRequest.getMentor().getId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        mentoringRequest.updateStatus(status);

        // 멘티에게 상태 변경 알림 메일 발송
        emailService.sendNotification(
            mentoringRequest.getMentee().getEmail(),
            "멘토링 신청 상태가 변경되었습니다.",
            "멘토링 상태 변경 알림",
            mentoringRequest.getMentor().getName() + " 멘토님이 신청을 [" + status.getDescription() + "] 하셨습니다."
        );

        if (status == MentoringStatus.COMPLETED) {
            gamificationService.awardPoints(mentoringRequest.getMentor().getId(), 50, yc.sw3.backend.domain.gamification.PointReason.MENTORING_COMPLETED);
            gamificationService.awardPoints(mentoringRequest.getMentee().getId(), 10, yc.sw3.backend.domain.gamification.PointReason.MENTORING_COMPLETED);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional
    public void expireOldRequests() {
        LocalDateTime limit = LocalDateTime.now().minusDays(3);
        List<MentoringRequest> expiredRequests = mentoringRequestRepository.findAllByStatusAndCreatedAtBefore(
                MentoringStatus.REQUESTED, limit);
        
        for (MentoringRequest request : expiredRequests) {
            request.updateStatus(MentoringStatus.REJECTED); // 또는 별도의 EXPIRED 상태 추가 가능
            emailService.sendNotification(
                request.getMentee().getEmail(),
                "멘토링 신청이 기한 만료되었습니다.",
                "멘토링 신청 만료 알림",
                request.getMentor().getName() + " 멘토님에게 보낸 신청이 3일 동안 응답이 없어 자동 취소되었습니다."
            );
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
