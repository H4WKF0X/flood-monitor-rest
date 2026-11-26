package com.example.floodmonitor.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private boolean running = false;

    int port;
    ServerBuilder<?> serverBuilder;
    List<BindableService> serviceList;
    Server server;

    public GrpcServerLifecycle(@Value("${grpc.server.port}") int port,
                               ServerBuilder<?> serverBuilder, List<BindableService> serviceList) {
        this.port = port;
        this.serverBuilder = serverBuilder;
        this.serviceList = serviceList;
    }
    @Override
    public void start() {
        server = serverBuilder.addServices(serviceList.stream().map(BindableService::bindService).toList()).build();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start gRPC server on port " + port, e);
        }
        logger.info("gRPC server started on port {}", server.getPort());
        running = true;
    }

    @Override
    public void stop() {
        try {
            server.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        server.shutdownNow();
        logger.info("gRPC server stopped");
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
