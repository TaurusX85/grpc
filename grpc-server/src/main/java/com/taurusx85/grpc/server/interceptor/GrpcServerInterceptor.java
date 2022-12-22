package com.taurusx85.grpc.server.interceptor;

import com.taurusx85.grpc.common.AppContext;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;

import static com.taurusx85.grpc.common.GrpcConstants.*;

@Slf4j
@GrpcGlobalServerInterceptor
public class GrpcServerInterceptor implements ServerInterceptor {


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String grpcMethodName = call.getMethodDescriptor().getFullMethodName();

        Context newContext = Context.current()
                                    .withValue(REQUEST_CONTEXT,  headers.get(REQUEST_ID_HEADER_KEY));

        return new ContextServerCallListener<>(next.startCall(call, headers), newContext, grpcMethodName);
    }



    @Slf4j
    static class ContextServerCallListener<ReqT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {

        private final Context context;
        private final String grpcMethodName;

        protected ContextServerCallListener(ServerCall.Listener<ReqT> delegate, Context context, String grpcMethodName) {
            super(delegate);
            this.context = context;
            this.grpcMethodName = grpcMethodName;
        }

        @Override
        public void onHalfClose() {                 //  If stream - need to use  'onMessage'
            Context previous = setUpContext();
            try {
                log.debug("Called: '" + grpcMethodName);
                super.onHalfClose();
            } catch (Exception e) {
                log.error(e.toString());
            } finally {
                cleanContext(previous);
            }
        }


        private void cleanContext(Context previous) {
            context.detach(previous);
            AppContext.clean();
            MDC.clear();
        }

        private Context setUpContext() {
            Context previous = context.attach();
            AppContext.setRequestId(REQUEST_CONTEXT.get());
            MDC.put(REQUEST_ID, AppContext.getRequestId());
            return previous;
        }
    }

}
