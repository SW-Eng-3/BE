package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.mentoring.MentoringStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class MentoringDto {

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ApplyRequest {
        private UUID mentorId;
        private String message;
    }

    @Getter @Builder @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String mentorName;
        private String menteeName;
        private String message;
        private MentoringStatus status;
        private LocalDateTime createdAt;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ScheduleRequest {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
