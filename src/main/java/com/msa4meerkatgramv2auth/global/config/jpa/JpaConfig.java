package com.msa4meerkatgramv2auth.global.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 이제 JPA Auditing 기능은 이 클래스가 담당
}
