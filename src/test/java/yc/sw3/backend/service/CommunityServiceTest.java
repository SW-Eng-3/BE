package yc.sw3.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yc.sw3.backend.domain.community.Post;
import yc.sw3.backend.domain.community.PostRepository;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;
import yc.sw3.backend.dto.PostDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @InjectMocks
    private CommunityService communityService;

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GamificationService gamificationService;
    @Mock
    private yc.sw3.backend.domain.community.CommentRepository commentRepository;
    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        UUID authorId = UUID.randomUUID();
        PostDto.CreateRequest request = PostDto.CreateRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        User author = User.builder().id(authorId).name("작성자").build();
        Post post = Post.builder().id(UUID.randomUUID()).build();

        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(postRepository.save(any())).willReturn(post);

        UUID postId = communityService.createPost(authorId, request);

        assertThat(postId).isNotNull();
        verify(postRepository, times(1)).save(any());
        verify(gamificationService, times(1)).awardPoints(eq(authorId), anyInt(), any());
    }

    @Test
    @DisplayName("댓글 추가 및 알림 발송")
    void addComment_Success() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        User author = User.builder().id(authorId).email("author@yc.ac.kr").build();
        Post post = Post.builder().id(postId).author(author).title("Title").build();
        User commenter = User.builder().id(userId).name("Commenter").build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(userId)).willReturn(Optional.of(commenter));

        communityService.addComment(postId, userId, "Comment Content");

        verify(commentRepository, times(1)).save(any());
        verify(emailService, times(1)).sendNotification(eq(author.getEmail()), anyString(), anyString(), anyString());
        verify(gamificationService, times(1)).awardPoints(eq(userId), anyInt(), any());
    }
}
