package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.report.ReportReason;
import yc.sw3.backend.domain.report.ReportStatus;
import yc.sw3.backend.domain.report.ReportTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportDto {

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private ReportTargetType targetType;
        private UUID targetId;
        private ReportReason reason;
        private String description;
    }

    @Getter @Builder @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID reporterId;
        private String reporterName;
        private ReportTargetType targetType;
        private UUID targetId;
        private ReportReason reason;
        private String reasonDescription;
        private String description;
        private ReportStatus status;
        private String statusDescription;
        private LocalDateTime createdAt;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ProcessRequest {
        private ReportStatus status;
    }
}
