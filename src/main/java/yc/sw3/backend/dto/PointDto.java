package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.gamification.PointReason;
import java.time.LocalDateTime;
import java.util.UUID;

public class PointDto {

    @Getter @Builder @AllArgsConstructor
    public static class SummaryResponse {
        private int totalPoints;
    }

    @Getter @Builder @AllArgsConstructor
    public static class HistoryResponse {
        private UUID id;
        private int amount;
        private PointReason reason;
        private String reasonDescription;
        private LocalDateTime createdAt;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class TestAwardRequest {
        private int amount;
        private PointReason reason;
    }
}
