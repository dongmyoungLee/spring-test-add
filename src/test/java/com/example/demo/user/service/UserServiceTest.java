package com.example.demo.user.service;

import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.domain.exception.ResourceNotFoundException;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserUpdate;
import com.example.demo.user.infrastructure.UserEntity;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
// 다 같이 실행하면 실패함.. 두 번째 테스트를 실행할 때 데이터가 이미 있어서 충돌이 발생함. 따라서 정리가 필요함.
// @Sql("/sql/user-repository-test-data.sql")
@SqlGroup({
    @Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
})
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void getByEmail_은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        String email = "kok202@naver.com";

        // when
        UserEntity result = userService.getByEmail(email);

        // then
        assertThat(result.getNickname()).isEqualTo("kok202");
    }

    @Test
    void getByEmail_은_PENDING_상태인_유저를_찾아올_수_없다() {
        // given
        String email = "kok203@naver.com";

        // when

        // then
        assertThatThrownBy(() -> {
            UserEntity result = userService.getByEmail(email);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        // when
        UserEntity result = userService.getById(1);

        // then
        assertThat(result.getNickname()).isEqualTo("kok202");
    }

    @Test
    void getById_는_PENDING_상태인_유저를_찾아올_수_없다() {
        // given

        // when

        // then
        assertThatThrownBy(() -> {
            UserEntity result = userService.getById(2);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userCreaetDTO_를_이용하여_유저를_생성_할_수_있다() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .email("kok202@naver.com")
                .address("seoul2")
                .nickname("nick")
                .build();

        // email 발송 Dummy로 대체..
        BDDMockito.doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // when
        UserEntity result = userService.create(userCreate);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    void updateDto_를_이용하여_유저를_수정할_수_있다() {
        // given
        UserUpdate userUpdate = UserUpdate.builder()
                .address("change")
                .nickname("change")
                .build();

        // email 발송 Dummy로 대체..
        BDDMockito.doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // when
        userService.update(1, userUpdate);

        // then
        UserEntity user = userService.getById(1);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getAddress()).isEqualTo("change");
        assertThat(user.getNickname()).isEqualTo("change");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        // given
        // when
        userService.login(1);

        // then
        UserEntity user = userService.getById(1);

        assertThat(user.getLastLoginAt()).isGreaterThan(0L);
    }

    @Test
    void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬_수_있다() {
        // given

        // when
        userService.verifyEmail(2, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        // then
        UserEntity user = userService.getById(2);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_에러를_던진다() {
        // given

        // when

        // then
        assertThatThrownBy(() -> {
            userService.verifyEmail(2, "aaaaaaaa");
        }).isInstanceOf(CertificationCodeNotMatchedException.class);
    }


}
