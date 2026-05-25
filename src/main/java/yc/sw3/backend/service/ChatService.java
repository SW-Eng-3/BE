package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.chat.*;
import yc.sw3.backend.domain.user.Role;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.ChatDto;
import yc.sw3.backend.dto.PageResponse;

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
        User student = getUser(userId, "사용자를 찾을 수 없습니다.");
        User senior = getUser(seniorId, "시니어를 찾을 수 없습니다.");

        if (student.getId().equals(senior.getId())) {
            throw new IllegalArgumentException("자기 자신과는 채팅방을 만들 수 없습니다.");
        }
        if (senior.getRole() == Role.STUDENT) {
            throw new IllegalArgumentException("졸업생, 교수 또는 관리자만 시니어로 선택될 수 있습니다.");
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
        User user = getUser(userId, "사용자를 찾을 수 없습니다.");
        return chatRoomRepository.findMyRoomsOrderByLatest(user).stream()
                .map(room -> toRoomResponse(room, false))
                .collect(Collectors.toList());
    }

    public ChatDto.RoomResponse getRoom(UUID userId, UUID roomId) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        return toRoomResponse(room, true);
    }

    public PageResponse<ChatDto.MessageResponse> getMessages(UUID userId, UUID roomId, Pageable pageable) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        Page<ChatMessage> messages = chatMessageRepository.findByRoomOrderByCreatedAtAsc(room, pageable);
        return PageResponse.of(messages.map(this::toMessageResponse));
    }

    @Transactional
    public ChatDto.MessageResponse saveMessage(UUID userId, UUID roomId, String content) {
        ChatRoom room = getRoomWithAccess(userId, roomId);
        User sender = getUser(userId, "발신자를 찾을 수 없습니다.");
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
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    private ChatRoom getRoomWithAccess(UUID userId, UUID roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다.");
        }
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        if (!room.hasParticipant(userId)) {
            throw new IllegalStateException("해당 채팅방에 접근할 권한이 없습니다.");
        }
        return room;
    }

    private String normalizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다.");
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("메시지 내용은 1000자 이내여야 합니다.");
        }
        return normalized;
    }

    private ChatDto.RoomResponse toRoomResponse(ChatRoom room, boolean includeMessages) {
        List<ChatDto.MessageResponse> messages = includeMessages
                ? chatMessageRepository.findByRoomOrderByCreatedAtAsc(room, org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
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
