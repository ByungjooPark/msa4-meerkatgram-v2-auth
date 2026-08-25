package com.msa4meerkatgramv2auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
// @EnableJpaAuditing  // << 테스트 코드 작성을 위해 `com.msa4meerkatgramv2auth.global.config.jpa.JpaConfig` 로 이전
public class Msa4MeerkatgramV2AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(Msa4MeerkatgramV2AuthApplication.class, args);
    }

}
