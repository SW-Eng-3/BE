package yc.sw3.backend.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import yc.sw3.backend.config.security.JwtTokenProvider;
import yc.sw3.backend.dto.ChatDto;
import yc.sw3.backend.service.ChatService;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    private final Map<UUID, Set<WebSocketSession>> sessionsByRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Map<String, String> query = parseQuery(session.getUri());
            String token = query.get("token");
            String roomIdValue = query.get("roomId");

            if (token == null || roomIdValue == null || !jwtTokenProvider.validateToken(token)) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("유효하지 않은 채팅 웹소켓 인증 정보입니다."));
                return;
            }

            UUID userId = UUID.fromString(jwtTokenProvider.getUserId(token));
            UUID roomId = UUID.fromString(roomIdValue);

            chatService.getRoom(userId, roomId);

            session.getAttributes().put("userId", userId);
            session.getAttributes().put("roomId", roomId);
            sessionsByRoom.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(session);
        } catch (RuntimeException e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("유효하지 않은 채팅 웹소켓 요청입니다."));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        UUID roomId = (UUID) session.getAttributes().get("roomId");

        if (userId == null || roomId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("채팅 웹소켓 세션이 인증되지 않았습니다."));
            return;
        }

        try {
            ChatDto.MessageRequest request = objectMapper.readValue(message.getPayload(), ChatDto.MessageRequest.class);
            ChatDto.MessageResponse response = chatService.saveMessage(userId, roomId, request.getContent());
            broadcast(roomId, objectMapper.writeValueAsString(response));
        } catch (RuntimeException e) {
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void broadcast(UUID roomId, String payload) {
        Set<WebSocketSession> sessions = sessionsByRoom.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage message = new TextMessage(payload);
        for (WebSocketSession target : sessions) {
            if (!target.isOpen()) {
                continue;
            }
            try {
                synchronized (target) {
                    target.sendMessage(message);
                }
            } catch (IOException e) {
                removeSession(target);
            }
        }
    }

    private void removeSession(WebSocketSession session) {
        Object roomId = session.getAttributes().get("roomId");
        if (roomId instanceof UUID id) {
            Set<WebSocketSession> sessions = sessionsByRoom.get(id);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionsByRoom.remove(id);
                }
            }
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        if (!session.isOpen()) {
            return;
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "ERROR",
                "message", message != null ? message : "채팅 메시지 전송에 실패했습니다."
        ))));
    }

    private Map<String, String> parseQuery(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            return Map.of();
        }
        return Arrays.stream(uri.getQuery().split("&"))
                .map(part -> part.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8),
                        (left, right) -> right
                ));
    }
}
