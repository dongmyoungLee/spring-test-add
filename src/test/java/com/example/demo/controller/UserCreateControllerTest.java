package com.example.demo.controller;

import com.example.demo.model.dto.UserCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserCreateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 사용자는_회원_가입을_할_수있고_회원가입된_사용자는_PENDING_상태이다() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("kok202@naver.com")
                .nickname("nick")
                .address("add")
                .build();

        // @MockBean을 사용하여 생성된 Mock 객체는 모든 메서드 호출에 대해 기본적으로 아무런 동작도 수행하지 않고 null 또는 기본값을 반환합니다.
        // 즉, mailSender.send(any(SimpleMailMessage.class)) 메서드가 호출되더라도 Mock 객체는 아무런 예외도 발생시키지 않고 단순히 메서드 호출을 무시합니다.
        BDDMockito.doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // when

        //then
        mockMvc.perform(post("/api/users")
                        .header("EMAIL", "kok202@naver.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("kok202@naver.com"))
                .andExpect(jsonPath("$.nickname").value("nick"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
