package com.example.floodmonitor;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class InProcessGrpcConfig {
    public static final String NAME = "ServerName";

    @Bean
    @Primary
    public ServerBuilder<?> testServerBuilder() {
        return InProcessServerBuilder.forName(NAME).directExecutor();
    }
}
