package com.taurusx85.grpc.client.config;

import com.taurusx85.grpc.client.interceptor.GrpcClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

@Component
public class GrpcChannelManager {

    @Getter
    private final ManagedChannel serverChannel;


    public GrpcChannelManager(@Value("${local-server.url}") String url,
                              @Value("${local-server.port}") int port) {
        this.serverChannel = ManagedChannelBuilder.forAddress(url, port)
                                                  .intercept(new GrpcClientInterceptor())
                                                  .usePlaintext()
                                                  .defaultLoadBalancingPolicy("pick_first")  // tenant_lb, round_robin
                                                  .defaultServiceConfig(getConfig())
                                                  .enableRetry()
                                                  .build();
    }


//  https://github.com/grpc/grpc-proto/blob/master/grpc/service_config/service_config.proto#L377
//  https://github.com/grpc/grpc-java/blob/v1.35.0/examples/src/main/resources/io/grpc/examples/retrying/retrying_service_config.json
    private Map<String, Object> getConfig() {
        Map<String, Object> name = new HashMap<>();
        name.put("service", "");
        name.put("method", "");

        Map<String, Object> retryPolicy = new HashMap<>();
        retryPolicy.put("maxAttempts", 5.0);
        retryPolicy.put("initialBackoff", "0.1s");
        retryPolicy.put("maxBackoff", "1s");
        retryPolicy.put("backoffMultiplier", 2.0);
        retryPolicy.put("retryableStatusCodes", singletonList("UNAVAILABLE"));

        Map<String, Object> methodConfig = new HashMap<>();
        methodConfig.put("name", singletonList(name));
//        methodConfig.put("timeout", "5s");
        methodConfig.put("retryPolicy", retryPolicy);
        methodConfig.put("wait_for_ready", true);

        Map<String, Object> config = new HashMap<>();
        config.put("methodConfig", singletonList(methodConfig));

        return config;
    }

}
