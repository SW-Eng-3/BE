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
import static org.mockito.ArgumentMatchers.any;
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
    }
}
