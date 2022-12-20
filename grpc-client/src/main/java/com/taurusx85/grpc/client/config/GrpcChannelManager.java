package com.taurusx85.grpc.client.config;

import com.taurusx85.grpc.client.interceptor.GrpcClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcChannelManager {

    @Getter
    private final ManagedChannel serverChannel;


    public GrpcChannelManager(@Value("${local-server.url}") String url,
                              @Value("${local-server.port}") int port) {
        this.serverChannel = ManagedChannelBuilder.forAddress(url, port)
                                                  .intercept(new GrpcClientInterceptor())
                                                  .usePlaintext()
                                                  .build();
    }
}
