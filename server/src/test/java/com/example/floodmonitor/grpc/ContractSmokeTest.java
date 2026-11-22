package com.example.floodmonitor.grpc;

import com.example.floodmonitor.grpc.test.PingRequest;
import com.example.floodmonitor.grpc.test.PingResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractSmokeTest {
    @Test
    void pingRequestSmokeTest() {
        PingRequest request = PingRequest.newBuilder().setIp("127.0.0.1").build();
        assertThat(request.getIp()).isEqualTo("127.0.0.1");
    }

    @Test
    void pingResponseSmokeTest() {
        PingResponse response = PingResponse.newBuilder().setReached(true).build();
        assertThat(response.getReached()).isTrue();
    }
}
