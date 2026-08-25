package com.msa4meerkatgramv2auth.domain.auth.controller;

import com.msa4meerkatgramv2auth.domain.auth.request.LoginRequestDTO;
import com.msa4meerkatgramv2auth.domain.auth.request.RegistrationRequestDTO;
import com.msa4meerkatgramv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4meerkatgramv2auth.domain.auth.service.AuthService;
import com.msa4meerkatgramv2auth.global.config.openapi.CustomApiResponse;
import com.msa4meerkatgramv2auth.global.cookie.CookieManager;
import com.msa4meerkatgramv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4meerkatgramv2auth.global.response.GlobalResponseDTO;
import com.msa4meerkatgramv2auth.global.response.constant.CustomResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 API", description = "인증 담당")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CookieManager cookieManager;

    @Operation(summary = "로그인 처리", description = "이메일과 비밀번호로 로그인")
    @SecurityRequirements
    @CustomApiResponse(value = {
        CustomResponseCode.NOT_REGISTERED_ERROR,
        CustomResponseCode.INVALID_PARAMETER_ERROR,
        CustomResponseCode.DB_ERROR,
        CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/login")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> login(
        @Valid @RequestBody LoginRequestDTO loginRequestDTO,
        HttpServletResponse response
    ) {
        AuthResponseDTO result = authService.login(loginRequestDTO);

        cookieManager.setRefreshTokenToCookie(response, result.refreshToken());

        return ResponseEntity.ok(GlobalResponseDTO.success(result));
    }

    @Operation(summary = "로그아웃 처리")
    @SecurityRequirement(name = "bearerAuth")
    @CustomApiResponse(value = {
        CustomResponseCode.UNAUTHENTICATED_ERROR,
        CustomResponseCode.INVALID_TOKEN_ERROR,
        CustomResponseCode.DB_ERROR,
        CustomResponseCode.SYSTEM_ERROR
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<GlobalResponseDTO<Void>> logout(
        HttpServletResponse response,
        Authentication authentication
    ) {
        long userId = Long.parseLong(authentication.getName());

        authService.logout(userId);

        // Cookie에 저장한 리프래시토큰 파기
        cookieManager.removeRefreshTokenToCookie(response);

        return ResponseEntity.ok(GlobalResponseDTO.success());
    }

    @Operation(summary = "토큰 재발급 처리")
    @SecurityRequirement(name = "cookieRefreshToken")
    @CustomApiResponse(value = {
        CustomResponseCode.INVALID_TOKEN_ERROR,
        CustomResponseCode.DB_ERROR,
        CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/reissue-token")
    public ResponseEntity<GlobalResponseDTO<AuthResponseDTO>> reissue(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // 쿠키 리프래시 토큰 획득
        String refreshToken = cookieManager.getRefreshTokenToCookie(request)
            .orElseThrow(() -> new InvalidTokenException("리프레시 토큰 없음"));

        AuthResponseDTO result = authService.reissue(refreshToken);
        cookieManager.setRefreshTokenToCookie(response, result.refreshToken());

        return ResponseEntity.ok(GlobalResponseDTO.success(result));
    }

    @Operation(summary = "회원가입 처리")
    @SecurityRequirements
    @CustomApiResponse(value = {
        CustomResponseCode.INVALID_PARAMETER_ERROR,
        CustomResponseCode.DB_ERROR,
        CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/registration")
    public ResponseEntity<GlobalResponseDTO<Void>> auth(
        @Valid @RequestBody RegistrationRequestDTO registrationRequestDTO
    ) {
        authService.registration(registrationRequestDTO);
        return ResponseEntity.ok(GlobalResponseDTO.success());
    }
}
