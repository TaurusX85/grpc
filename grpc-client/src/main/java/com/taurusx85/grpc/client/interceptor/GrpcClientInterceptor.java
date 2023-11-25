package com.taurusx85.grpc.client.interceptor;

import com.taurusx85.grpc.common.AppContext;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.apache.commons.lang3.StringUtils;

import static com.taurusx85.grpc.common.GrpcConstants.REQUEST_ID_HEADER_KEY;

/**
 *  Interceptor forwards RequestId from Context to header
 */
@Slf4j
@GrpcGlobalClientInterceptor
public class GrpcClientInterceptor implements ClientInterceptor {


    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        log.debug("Called: {}", method.getFullMethodName());
        return new EnrichedHeadersClientCall<>(next.newCall(method, callOptions)) ;
    }


    @Slf4j
    static class EnrichedHeadersClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        EnrichedHeadersClientCall(ClientCall<ReqT, RespT> delegate) {
            super(delegate);
        }

        @Override
        public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
            headers.put(REQUEST_ID_HEADER_KEY,  AppContext.getRequestId());
            super.start(responseListener, headers);
        }

    }

}
