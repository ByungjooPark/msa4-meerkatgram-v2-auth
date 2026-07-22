package com.msa4meerkatgramv2auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
public class Msa4MeerkatgramV2AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(Msa4MeerkatgramV2AuthApplication.class, args);
    }

}
