package com.msa4meerkatgramv2auth.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa4meerkatgramv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4meerkatgramv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4meerkatgramv2auth.domain.auth.service.AuthService;
import com.msa4meerkatgramv2auth.domain.user.response.UserResponseDTO;
import com.msa4meerkatgramv2auth.global.cookie.CookieManager;
import com.msa4meerkatgramv2auth.global.error.custom.business.NotRegisteredException;
import com.msa4meerkatgramv2auth.global.security.constant.RolePolicy;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieManager cookieManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOGIN_URI = "/api/auth/login";

    private LoginRequestDTO validRequest() {
        return new LoginRequestDTO("test@test.com", "qwer1234");
    }

    private AuthResponseDTO successResponse() {
        UserResponseDTO userResponseDTO = new UserResponseDTO(
            1L, "test@test.com", "nickname", RolePolicy.SUPER, null, LocalDateTime.now()
        );
        return new AuthResponseDTO(userResponseDTO, "access-token", "refresh-token");
    }

    @Test
    @DisplayName("로그인 성공 시 200과 함께 응답 데이터를 반환하고, 리프레시 토큰을 쿠키에 저장한다")
    void login_success() throws Exception {
        AuthResponseDTO response = successResponse();
        given(authService.login(any(LoginRequestDTO.class))).willReturn(response);

        mockMvc.perform(
                post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest()))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00"))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.user.email").value("test@test.com"))
            .andExpect(jsonPath("$.data.refreshToken").doesNotExist());

        verify(authService).login(eq(validRequest()));
        verify(cookieManager).setRefreshTokenToCookie(any(HttpServletResponse.class), eq("refresh-token"));
    }

    @Test
    @DisplayName("email이 비어있으면 400과 INVALID_PARAMETER_ERROR를 반환한다")
    void login_fail_blankEmail() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "qwer1234");

        mockMvc.perform(post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("E21"));

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("email 형식이 유효하지 않으면 400과 INVALID_PARAMETER_ERROR를 반환한다")
    void login_fail_invalidEmailFormat() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("invalid-email", "qwer1234");

        mockMvc.perform(post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("E21"));

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("password가 비어있으면 400과 INVALID_PARAMETER_ERROR를 반환한다")
    void login_fail_blankPassword() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@test.com", "");

        mockMvc.perform(post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("E21"));

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("password 형식이 유효하지 않으면 400과 INVALID_PARAMETER_ERROR를 반환한다")
    void login_fail_invalidPasswordFormat() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@test.com", "ab");

        mockMvc.perform(post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("E21"));

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("미가입 이메일이거나 비밀번호가 일치하지 않으면 401과 NOT_REGISTERED_ERROR를 반환한다")
    void login_fail_notRegistered() throws Exception {
        willThrow(new NotRegisteredException("아이디와 비밀번호를 확인해주세요."))
            .given(authService).login(any(LoginRequestDTO.class));

        mockMvc.perform(
                post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest()))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("E01"));

        verify(cookieManager, never()).setRefreshTokenToCookie(any(), any());
    }

    @Test
    @DisplayName("요청 바디가 없으면 400을 반환한다")
    void login_fail_emptyBody() throws Exception {
        mockMvc.perform(
                post(LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    // @Test
    // @DisplayName("DB 예외 발생 시 500과 DB_ERROR를 반환한다")
    // void login_fail_dbError() throws Exception {
    //     willThrow(new DataAccessResourceFailureException("db connection failed"))
    //         .given(authService).login(any(LoginRequestDTO.class));
    //
    //     mockMvc.perform(post(LOGIN_URI)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(validRequest())))
    //         .andExpect(status().isInternalServerError())
    //         .andExpect(jsonPath("$.code").value("E80"));
    // }
    //
    // @Test
    // @DisplayName("예상치 못한 예외 발생 시 500과 SYSTEM_ERROR를 반환한다")
    // void login_fail_systemError() throws Exception {
    //     willThrow(new RuntimeException("unexpected"))
    //         .given(authService).login(any(LoginRequestDTO.class));
    //
    //     mockMvc.perform(post(LOGIN_URI)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(validRequest())))
    //         .andExpect(status().isInternalServerError())
    //         .andExpect(jsonPath("$.code").value("E99"));
    // }
}