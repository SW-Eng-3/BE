package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.chat.ChatRoomStatus;
import yc.sw3.backend.domain.chat.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomRequest {
        private UUID seniorId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageRequest {
        private UUID roomId;
        private String content;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RoomResponse {
        private UUID roomId;
        private UUID studentId;
        private String studentName;
        private UUID seniorId;
        private String seniorName;
        private ChatRoomStatus status;
        private String lastMessage;
        private LocalDateTime lastMessageAt;
        private List<MessageResponse> messages;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MessageResponse {
        private UUID messageId;
        private UUID roomId;
        private UUID senderId;
        private String senderName;
        private String content;
        private MessageType messageType;
        private LocalDateTime createdAt;
    }
}
