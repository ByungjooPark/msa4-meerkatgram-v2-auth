package com.msa4meerkatgramv2auth.domain.auth.service;

import com.msa4meerkatgramv2auth.domain.auth.repository.AuthRepository;
import com.msa4meerkatgramv2auth.domain.auth.response.AuthResponseDTO;
import com.msa4meerkatgramv2auth.domain.user.entity.User;
import com.msa4meerkatgramv2auth.domain.user.request.LoginRequestDTO;
import com.msa4meerkatgramv2auth.global.error.custom.business.InvalidTokenException;
import com.msa4meerkatgramv2auth.global.error.custom.business.NotRegisteredException;
import com.msa4meerkatgramv2auth.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 유저 정보를 획득 & 유저 가입 여부를 체크
        User user = authRepository.findByEmail(loginRequestDTO.email())
            .orElseThrow(() -> new NotRegisteredException("아이디와 비밀번호를 확인해주세요."));

        // 탈퇴 여부 확인
        if(user.getIsWithdraw()) {
            throw new NotRegisteredException("탈퇴한 회원입니다.");
        }

        // 비밀번호 체크
        if(!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new NotRegisteredException("아이디와 비밀번호를 확인해주세요.");
        }

        return this.generateAuthentication(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO reissue(String refreshToken) {
        long userId = Long.parseLong(jwtProvider.extractClaims(refreshToken).getSubject());

        // 유저 획득 및 가입 여부 확인
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("유효하지 않은 회원의 토큰입니다."));

        // 비로그인 상태 확인
        if(user.getRefreshToken() == null) {
            throw new InvalidTokenException("비로그인 상태입니다.");
        }

        // 리프래시 토큰 일치 확인
        if(!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("토큰 불일치입니다.");
        }

        // 인증 정보생성 및 리턴
        return this.generateAuthentication(user);
    }

    private AuthResponseDTO generateAuthentication(User user) {
        // 토큰생성
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 리프래시토큰 DB 저장 처리
        user.setRefreshToken(refreshToken);
        authRepository.save(user);

        // --------- edt: DTO 변경 및 HTTP 처리 컨트롤러로 이관에 따른 변경 start ---------
        // 리프래시 토큰 cookie에 저장
        // cookieManager.setRefreshTokenToCookie(response, refreshToken);

        // return AuthResponseDTO.from(user, accessToken);
        return AuthResponseDTO.from(user, accessToken, refreshToken);
        // --------- edt: DTO 변경 및 HTTP 처리 컨트롤러로 이관에 따른 변경 end ---------
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout(long userId) {
        // 유저 정보 획득
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("유효하지 않는 회원입니다."));

        // DB에 저장한 리프래시 토큰 파기
        user.setRefreshToken(null);
        authRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long userId) {
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotRegisteredException("유효하지 않은 회원입니다."));

        user.setIsWithdraw(true);
        user.setWithdrawnAt(LocalDateTime.now());
    }
}
