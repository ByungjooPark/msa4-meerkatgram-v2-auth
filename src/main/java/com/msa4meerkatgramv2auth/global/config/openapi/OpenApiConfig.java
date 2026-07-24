package com.msa4meerkatgramv2auth.global.config.openapi;

import com.msa4meerkatgramv2auth.global.jwt.JwtConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";
    private static final String COOKIE_REFRESH_TOKEN = "cookieRefreshToken";

    @Bean
    public OpenAPI customOpenAPI(JwtConfig jwtConfig) {
        return new OpenAPI()
            .info(
              new Info()
                  .title("Meerkatgram Auth API") // 문서 제목
                  .description("Meerkatgram Auth REST API Document") // 문서 설명
                  .version("v1.0.0") // 문서 버전
            )
            .components(new Components()
                .addSecuritySchemes(
                    BEARER_AUTH,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(
                    COOKIE_REFRESH_TOKEN,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(jwtConfig.refreshTokenCookieName())
                )
            )
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
