package yc.sw3.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${custom.env-name:Production}")
    private String envName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🚀 " + "SWENG3 Backend API")
                        .description("현재 실행 환경: **" + envName + "**")
                        .version("1.0.0"));
    }
}
