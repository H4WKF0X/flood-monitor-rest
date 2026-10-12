package com.example.floodmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI floodMonitorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flood Monitor API")
                        .version("v1")
                        .description("""
                                REST API for river gauging stations. Provides station data, water level measurements, \
                                statistics and flood alerts.

                                Every endpoint returns JSON or XML depending on the `Accept` header. \
                                Errors use the `ApiError` schema in the requested format. \
                                Any endpoint can also return 406 (unsupported `Accept` header, always as JSON) \
                                and 500 (unexpected server error).

                                Units: water levels in cm, flow rate in m³/s, rainfall in mm/h, \
                                temperature in °C, battery level in %. Timestamps are ISO-8601 in UTC."""));
    }
}
