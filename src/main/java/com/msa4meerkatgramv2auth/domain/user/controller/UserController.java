package com.msa4meerkatgramv2auth.domain.user.controller;

import com.msa4meerkatgramv2auth.domain.auth.request.RegistrationRequestDTO;
import com.msa4meerkatgramv2auth.domain.user.UserService.UserService;
import com.msa4meerkatgramv2auth.global.config.openapi.CustomApiResponse;
import com.msa4meerkatgramv2auth.global.response.GlobalResponseDTO;
import com.msa4meerkatgramv2auth.global.response.constant.CustomResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 API", description = "유저 담당")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입 처리")
    @SecurityRequirements
    @CustomApiResponse(value = {
        CustomResponseCode.INVALID_PARAMETER_ERROR,
        CustomResponseCode.DB_ERROR,
        CustomResponseCode.DB_DUPLICATED_KEY_ERROR,
        CustomResponseCode.SYSTEM_ERROR
    })
    @PostMapping("/registration")
    public ResponseEntity<GlobalResponseDTO<Void>> registration(
        @Valid @RequestBody RegistrationRequestDTO registrationRequestDTO
    ) {
        userService.registration(registrationRequestDTO);
        return ResponseEntity.ok(GlobalResponseDTO.success());
    }
}
