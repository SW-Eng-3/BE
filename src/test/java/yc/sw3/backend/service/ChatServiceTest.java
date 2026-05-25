package yc.sw3.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yc.sw3.backend.domain.chat.*;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.ChatDto;
import yc.sw3.backend.dto.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private UserRepository userRepository;

    private User user;
    private ChatRoom room;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@yc.ac.kr")
                .name("User")
                .build();

        room = ChatRoom.builder()
                .id(UUID.randomUUID())
                .student(user)
                .senior(User.builder().id(UUID.randomUUID()).build())
                .build();
    }

    @Test
    @DisplayName("채팅 메시지 저장 성공")
    void saveMessage_Success() {
        String content = "Hello world";
        given(chatRoomRepository.findById(room.getId())).willReturn(Optional.of(room));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        
        ChatMessage savedMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .content(content)
                .room(room)
                .sender(user)
                .build();
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        ChatDto.MessageResponse response = chatService.saveMessage(user.getId(), room.getId(), content);

        assertThat(response.getContent()).isEqualTo(content);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅 메시지 목록 페이징 조회 성공")
    void getMessages_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID())
                .content("메시지")
                .room(room)
                .sender(user)
                .build();
        
        Page<ChatMessage> page = new PageImpl<>(List.of(message), pageable, 1);

        given(chatRoomRepository.findById(room.getId())).willReturn(Optional.of(room));
        given(chatMessageRepository.findByRoomOrderByCreatedAtAsc(eq(room), any(Pageable.class))).willReturn(page);

        PageResponse<ChatDto.MessageResponse> response = chatService.getMessages(user.getId(), room.getId(), pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(chatMessageRepository, times(1)).findByRoomOrderByCreatedAtAsc(eq(room), any(Pageable.class));
    }
}
