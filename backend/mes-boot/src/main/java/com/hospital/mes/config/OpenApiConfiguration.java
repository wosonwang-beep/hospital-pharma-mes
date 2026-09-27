package com.hospital.mes.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfiguration {
 @Bean OpenAPI mesOpenApi(){ return new OpenAPI().info(new Info().title("Hospital Pharmaceutical MES API").version("v1").description("MES V2.0 Foundation API")); }
}
