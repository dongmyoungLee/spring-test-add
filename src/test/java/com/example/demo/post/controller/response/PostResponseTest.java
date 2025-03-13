package com.example.demo.post.controller.response;

import com.example.demo.post.domain.Post;
import com.example.demo.user.controller.response.UserResponse;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostResponseTest {

    @Test
    public void Post로_응답을_생성할_수_있다() {
        //given
        Post post = Post.builder()
                .content("test")
                .writer(User.builder()
                        .email("kok202@naver.com")
                        .nickname("kok202")
                        .address("Seoul")
                        .status(UserStatus.ACTIVE)
                        .certificationCode("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                        .build())
                .build();

        //when
        PostResponse postResponse = PostResponse.from(post);

        //then

        assertThat(postResponse.getContent()).isEqualTo("test");
        assertThat(postResponse.getWriter().getEmail()).isEqualTo("kok202@naver.com");
    }
}
