package com.taurusx85.grpc.server.config;

import io.grpc.ServerInterceptor;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    /**
     * Forward uncaught exceptions to client
     * with contained context
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor statusInterceptor() {
        return TransmitStatusRuntimeExceptionInterceptor.instance();
    }

}
