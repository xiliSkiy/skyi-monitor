package com.skyi.visualization.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI描述信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SKYI可视化服务API")
                        .version("1.0.0")
                        .description("SKYI监控平台可视化服务API文档")
                        .termsOfService("https://skyi.com/terms")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 