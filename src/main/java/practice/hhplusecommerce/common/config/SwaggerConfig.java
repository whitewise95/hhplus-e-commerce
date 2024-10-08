package practice.hhplusecommerce.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  
  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
        .title("Springdoc Swagger")
        .description("Springdoc을 사용한 Swagger UI")
        .version("1.0.0")
        .license(new License().name("Apache 2.0").url("http://springdoc.org"));

    // SecuritySecheme명
    String jwtSchemeName = "jwtAuth";
    // API 요청헤더에 인증정보 포함
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
    // SecuritySchemes 등록
    Components components = new Components()
        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP) // HTTP 방식
            .scheme("bearer")
            .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자(Optional)

    return new OpenAPI()
        .info(info)
        .addSecurityItem(securityRequirement)
        .components(components);
  }
}
