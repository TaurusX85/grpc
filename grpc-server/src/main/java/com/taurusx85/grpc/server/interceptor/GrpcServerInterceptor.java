package com.taurusx85.grpc.server.interceptor;

import com.taurusx85.grpc.common.AppContext;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;

import static com.taurusx85.grpc.common.GrpcConstants.*;

/**
 * Set up RequestId on Context before call
 * and clear after
 */
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

        /**
         *      -------------------------- Blocking -------------------------------------
         *
         *         onMessage               * 1
         *         onMessage.finally       * 1
         *         onHalfClose             * 1
         *         SERVICE                 * 1
         *         onHalfClose.finally     * 1
         *         onComplete              * 1
         *
         *     -------------------------- Streaming -------------------------------------
         *
         *         onMessage			   * N
         *         SERVICE				   * N
         *         onMessage.finally	   * N
         *         onHalfClose             * 1
         *         onHalfClose.finally     * 1
         *         onComplete              * 1
         */

        //  If stream - need to use  'onMessage', otherwise 'onHalfClose' is enough
        @Override
        public void onHalfClose() {
            Context attachedContext = setUpContext();
            try {
                super.onHalfClose();
            } catch (Exception e) {
                log.error(e.toString());
            } finally {
                cleanContext(attachedContext);
            }
        }

        public void onMessage(ReqT message) {
            Context attachedContext = setUpContext();
            try {
                log.debug("Called: '" + grpcMethodName);
                super.onMessage(message);
            } catch (Exception e) {
                log.error(e.toString());
            } finally {
                cleanContext(attachedContext);
            }
        }



        private void cleanContext(Context previous) {
            context.detach(previous);
            AppContext.clean();
            MDC.clear();
        }

        private Context setUpContext() {
            Context attachedContext = context.attach();
            AppContext.setRequestId(REQUEST_CONTEXT.get());
            MDC.put(REQUEST_ID, AppContext.getRequestId());
            return attachedContext;
        }
    }

}
