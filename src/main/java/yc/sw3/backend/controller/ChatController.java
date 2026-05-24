package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.dto.ChatDto;
import yc.sw3.backend.service.ChatService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat", description = "1:1 real-time chat API")
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Create or get a senior chat room")
    @PostMapping("/rooms/senior")
    public ResponseEntity<ChatDto.RoomResponse> getOrCreateSeniorRoom(
            @AuthenticationPrincipal UUID userId,
            @RequestBody ChatDto.RoomRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateSeniorRoom(userId, request.getSeniorId()));
    }

    @Operation(summary = "Create or get a senior chat room by query parameter")
    @GetMapping("/rooms/senior")
    public ResponseEntity<ChatDto.RoomResponse> getOrCreateSeniorRoomByQuery(
            @AuthenticationPrincipal UUID userId,
            @RequestParam UUID seniorId) {
        return ResponseEntity.ok(chatService.getOrCreateSeniorRoom(userId, seniorId));
    }

    @Operation(summary = "Get my chat rooms")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatDto.RoomResponse>> getMyRooms(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(chatService.getMyRooms(userId));
    }

    @Operation(summary = "Get chat room detail")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatDto.RoomResponse> getRoom(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getRoom(userId, roomId));
    }

    @Operation(summary = "Get chat room messages")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatDto.MessageResponse>> getMessages(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getMessages(userId, roomId));
    }

    @Operation(summary = "Send chat room message without websocket")
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatDto.MessageResponse> sendMessage(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID roomId,
            @RequestBody ChatDto.MessageRequest request) {
        return ResponseEntity.ok(chatService.saveMessage(userId, roomId, request.getContent()));
    }
}
