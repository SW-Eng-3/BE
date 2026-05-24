package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.chat.*;
import yc.sw3.backend.domain.user.Role;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.ChatDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatDto.RoomResponse getOrCreateSeniorRoom(UUID userId, UUID seniorId) {
        User student = getUser(userId, "User not found");
        User senior = getUser(seniorId, "Senior not found");

        if (student.getId().equals(senior.getId())) {
            throw new IllegalArgumentException("Cannot create a chat room with yourself");
        }
        if (senior.getRole() == Role.STUDENT) {
            throw new IllegalArgumentException("Only graduates, professors, or admins can be selected as seniors");
        }

        ChatRoom room = chatRoomRepository.findByStudentAndSenior(student, senior)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .student(student)
                        .senior(senior)
                        .status(ChatRoomStatus.ACTIVE)
                        .build()));

        return toRoomResponse(room, true);
    }

    public List<ChatDto.RoomResponse> getMyRooms(UUID userId) {
        User user = getUser(userId, "User not found");
        return chatRoomRepository.findMyRoomsOrderByLatest(user).stream()
                .map(room -> toRoomResponse(room, false))
                .collect(Collectors.toList());
    }

    public ChatDto.RoomResponse getRoom(UUID userId, UUID roomId) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        return toRoomResponse(room, true);
    }

    public List<ChatDto.MessageResponse> getMessages(UUID userId, UUID roomId) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        return chatMessageRepository.findByRoomOrderByCreatedAtAsc(room).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatDto.MessageResponse saveMessage(UUID userId, UUID roomId, String content) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        User sender = getUser(userId, "Sender not found");
        String normalizedContent = normalizeContent(content);

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(normalizedContent)
                .messageType(MessageType.CHAT)
                .build());

        room.updateLastMessage(normalizedContent, message.getCreatedAt());

        return toMessageResponse(message);
    }

    private User getUser(UUID userId, String message) {
        if (userId == null) {
            throw new IllegalArgumentException("Authentication is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    private ChatRoom getRoomWithAccess(UUID userId, UUID roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room id is required");
        }
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        if (!room.hasParticipant(userId)) {
            throw new IllegalStateException("No permission to access this chat room");
        }
        return room;
    }

    private String normalizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content is required");
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message content must be 1000 characters or less");
        }
        return normalized;
    }

    private ChatDto.RoomResponse toRoomResponse(ChatRoom room, boolean includeMessages) {
        List<ChatDto.MessageResponse> messages = includeMessages
                ? chatMessageRepository.findByRoomOrderByCreatedAtAsc(room).stream()
                        .map(this::toMessageResponse)
                        .collect(Collectors.toList())
                : List.of();

        return ChatDto.RoomResponse.builder()
                .roomId(room.getId())
                .studentId(room.getStudent().getId())
                .studentName(room.getStudent().getName())
                .seniorId(room.getSenior().getId())
                .seniorName(room.getSenior().getName())
                .status(room.getStatus())
                .lastMessage(room.getLastMessage())
                .lastMessageAt(room.getLastMessageAt())
                .messages(messages)
                .build();
    }

    private ChatDto.MessageResponse toMessageResponse(ChatMessage message) {
        return ChatDto.MessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
