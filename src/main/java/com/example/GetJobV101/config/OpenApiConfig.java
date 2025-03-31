package com.example.GetJobV101.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 서버 URL 설정 (Swagger에서 사용하는 기본 주소)
        Server server = new Server();
        server.setUrl("https://getjob.world");
        server.setDescription("Production Server");

        // 🔐 JWT 인증용 SecurityScheme 정의
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)       // HTTP 인증 방식
                .scheme("bearer")                     // Bearer 방식 사용
                .bearerFormat("JWT");                 // 형식은 JWT


        // 🔐 이 인증 스키마를 모든 API에 기본 적용
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("GetJob API")
                        .version("v1")
                        .description("GetJob 서비스 Swagger 문서"))
                .servers(List.of(server))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", jwtScheme)) // 🔑 이름은 bearerAuth
                .addSecurityItem(securityRequirement); // 전체 API에 적용
    }
}
