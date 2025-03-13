package com.example.demo.user.service;

import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.domain.exception.ResourceNotFoundException;
import com.example.demo.mock.FakeMailSender;
import com.example.demo.mock.FakeUserRepository;
import com.example.demo.mock.TestClockHolder;
import com.example.demo.mock.TestUuidHolder;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserUpdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class UserServiceTest {
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void init() {
        FakeMailSender fakeMailSender = new FakeMailSender();
        FakeUserRepository fakeUserRepository = new FakeUserRepository();

        this.userServiceImpl = UserServiceImpl.builder()
                .userRepository(fakeUserRepository)
                .certificationService(new CertificationService(fakeMailSender))
                .clockHolder(new TestClockHolder(1678530673958L))
                .uuidHolder(new TestUuidHolder("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .build();

        fakeUserRepository.save(User.builder()
                .id(1L)
                .email("kok202@naver.com")
                .nickname("kok202")
                .address("Seoul")
                .certificationCode("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .status(UserStatus.ACTIVE)
                .lastLoginAt(0L)
                .build());
        fakeUserRepository.save(User.builder()
                .id(2L)
                .email("kok303@naver.com")
                .nickname("kok303")
                .address("Seoul")
                .certificationCode("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab")
                .status(UserStatus.PENDING)
                .lastLoginAt(0L)
                .build());
    }

    @Test
    void getByEmail_은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        String email = "kok202@naver.com";

        // when
        User result = userServiceImpl.getByEmail(email);

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
            User result = userServiceImpl.getByEmail(email);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        // when
        User result = userServiceImpl.getById(1);

        // then
        assertThat(result.getNickname()).isEqualTo("kok202");
    }

    @Test
    void getById_는_PENDING_상태인_유저를_찾아올_수_없다() {
        // given

        // when

        // then
        assertThatThrownBy(() -> {
            User result = userServiceImpl.getById(2L);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userCreaet_를_이용하여_유저를_생성_할_수_있다() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .email("kok202@naver.com")
                .address("seoul2")
                .nickname("nick")
                .build();
        // when
        User result = userServiceImpl.create(userCreate);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(result.getCertificationCode()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    }

    @Test
    void updateDto_를_이용하여_유저를_수정할_수_있다() {
        // given
        UserUpdate userUpdate = UserUpdate.builder()
                .address("change")
                .nickname("change")
                .build();

        // when
        userServiceImpl.update(1, userUpdate);

        // then
        User user = userServiceImpl.getById(1);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getAddress()).isEqualTo("change");
        assertThat(user.getNickname()).isEqualTo("change");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        // given
        // when
        userServiceImpl.login(1);

        // then
        User user = userServiceImpl.getById(1);

        assertThat(user.getLastLoginAt()).isEqualTo(1678530673958L);
    }

    @Test
    void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬_수_있다() {
        // given

        // when
        userServiceImpl.verifyEmail(1, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        // then
        User user = userServiceImpl.getById(1);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_에러를_던진다() {
        // given

        // when

        // then
        assertThatThrownBy(() -> {
            userServiceImpl.verifyEmail(2, "aaaaaaaa");
        }).isInstanceOf(CertificationCodeNotMatchedException.class);
    }


}
