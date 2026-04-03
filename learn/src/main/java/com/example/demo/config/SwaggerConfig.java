package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger3 / springdoc-openapi 配置
 * <p>访问地址：http://localhost:8080/swagger-ui.html</p>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud-Native Demo API")
                        .description("Spring Boot 3.5.x 云原生示例项目接口文档\n" +
                                "技术栈：PostgreSQL + MyBatis / MongoDB / Upstash Redis / OpenFeign / Sentry / Vercel EdgeConfig")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Demo Team")
                                .url("https://github.com/example/demo")
                                .email("dev@example.com"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                // Bearer Token 认证（可选，如需接入 JWT 直接启用）
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
