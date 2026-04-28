package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.community.PostCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostDto {

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private String title;
        private String content;
        private PostCategory category;
        private boolean isAnonymous;
    }

    @Getter @Builder @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String title;
        private String content;
        private String authorName;
        private PostCategory category;
        private boolean isAnonymous;
        private boolean isPinned;
        private LocalDateTime createdAt;
        private List<CommentResponse> comments;
    }

    @Getter @Builder @AllArgsConstructor
    public static class CommentResponse {
        private UUID id;
        private String content;
        private String authorName;
        private boolean isRecommended;
        private LocalDateTime createdAt;
    }
}
