package yc.sw3.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yc.sw3.backend.dto.PostDto;
import yc.sw3.backend.service.CommunityService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Community", description = "커뮤니티 및 게시 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "게시글 작성", description = "카테고리 및 익명 여부를 포함하여 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<UUID> createPost(@RequestHeader("X-User-Id") UUID authorId, @RequestBody PostDto.CreateRequest request) {
        return ResponseEntity.ok(communityService.createPost(authorId, request));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ResponseEntity<List<PostDto.Response>> getPosts() {
        return ResponseEntity.ok(communityService.getPosts());
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 내용 및 댓글 목록을 반환합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.Response> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(communityService.getPost(postId));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        communityService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전공 공지 고정", description = "교수 권한으로 게시글을 상단에 고정합니다.")
    @PatchMapping("/{postId}/pin")
    public ResponseEntity<Void> pinPost(@PathVariable UUID postId, @RequestParam boolean isPinned) {
        communityService.pinPost(postId, isPinned);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "답변(댓글) 작성")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> addComment(@PathVariable UUID postId, @RequestHeader("X-User-Id") UUID userId, @RequestBody String content) {
        communityService.addComment(postId, userId, content);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "우수 답변 지정", description = "졸업생 답변을 '우수 답변'으로 선정합니다.")
    @PatchMapping("/comments/{commentId}/recommend")
    public ResponseEntity<Void> recommendComment(@PathVariable UUID commentId, @RequestParam boolean isRecommended) {
        communityService.recommendComment(commentId, isRecommended);
        return ResponseEntity.ok().build();
    }
}
