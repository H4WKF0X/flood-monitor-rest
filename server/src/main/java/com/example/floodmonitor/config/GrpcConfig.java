package com.example.floodmonitor.config;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    private final int port;

    public GrpcConfig(@Value("${grpc.server.port}") int port) {
        this.port = port;
    }

    @Bean
    public ServerBuilder<?> serverBuilder() {
        return ServerBuilder.forPort(port);
    }

    @Bean
    public BindableService reflectionService() {
        return ProtoReflectionServiceV1.newInstance();
    }
}
