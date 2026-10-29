package fu.de190060.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Media Gateway API")
                .version("1.0.0")
                .description("MSS301 2026 Spring PE - single entry point to User Service and Post Service"));
    }
}
