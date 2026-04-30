package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.community.*;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.PostDto;
import yc.sw3.backend.domain.gamification.PointReason;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Transactional
    public UUID createPost(UUID authorId, PostDto.CreateRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isAnonymous(request.isAnonymous())
                .isPinned(false)
                .build();

        UUID postId = postRepository.save(post).getId();

        gamificationService.awardPoints(authorId, 10, PointReason.POST_CREATED);

        return postId;
    }

    public List<PostDto.Response> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PostDto.Response getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return convertToDto(post);
    }

    @Transactional
    public void deletePost(UUID postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    public void pinPost(UUID postId, boolean isPinned) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.updatePin(isPinned);
    }

    @Transactional
    public void addComment(UUID postId, UUID userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .isRecommended(false)
                .build();

        commentRepository.save(comment);

        gamificationService.awardPoints(userId, 3, PointReason.COMMENT_CREATED);
    }

    @Transactional
    public void recommendComment(UUID commentId, boolean isRecommended) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.updateRecommend(isRecommended);
    }

    private PostDto.Response convertToDto(Post post) {
        return PostDto.Response.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.isAnonymous() ? "익명" : post.getAuthor().getName())
                .category(post.getCategory())
                .isAnonymous(post.isAnonymous())
                .isPinned(post.isPinned())
                .createdAt(post.getCreatedAt())
                .comments(post.getComments().stream()
                        .map(c -> PostDto.CommentResponse.builder()
                                .id(c.getId())
                                .content(c.getContent())
                                .authorName(c.getUser().getName())
                                .isRecommended(c.isRecommended())
                                .createdAt(c.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
