package com.msa4meerkatgramv2auth.domain.user.UserService;

import com.msa4meerkatgramv2auth.domain.auth.request.RegistrationRequestDTO;
import com.msa4meerkatgramv2auth.domain.user.entity.User;
import com.msa4meerkatgramv2auth.domain.user.repogitory.UserRepository;
import com.msa4meerkatgramv2auth.global.config.jpa.JPAWithDeleted;
import com.msa4meerkatgramv2auth.global.error.custom.business.DuplicatedResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @JPAWithDeleted
    @Transactional(rollbackFor = Exception.class)
    public void registration(RegistrationRequestDTO registrationRequestDTO) {
        // 유저 정보를 획득 & 유저 가입 여부를 체크
        userRepository.findByEmail(registrationRequestDTO.email())
            .ifPresentOrElse(
                user -> {
                    if(user.getIsWithdraw()) {
                        user.setIsWithdraw(false);
                        user.setWithdrawnAt(null);
                        user.setRestoredAt(LocalDateTime.now());
                    } else {
                        throw new DuplicatedResourceException("이미 가입된 이메일 입니다.");
                    }
                },
                () -> {
                    User user = new User();
                    user.setEmail(registrationRequestDTO.email());
                    user.setPassword(passwordEncoder.encode(registrationRequestDTO.password()));
                    user.setNick(registrationRequestDTO.nick());
                    user.setProfile(registrationRequestDTO.profile());
                    userRepository.save(user);
                }
            );
    }
}
