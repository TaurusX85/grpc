package com.taurusx85.grpc.common;

import com.google.common.util.concurrent.FutureCallback;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.slf4j.MDC;

import static com.taurusx85.grpc.common.GrpcConstants.REQUEST_ID;

public abstract class CustomFutureCallback<T> implements FutureCallback<T> {

    protected final String requestId;


    public CustomFutureCallback(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public void onSuccess(@NullableDecl T result) {
        restoreContext();
        success(result);
    }


    @Override
    public void onFailure(Throwable t) {
        restoreContext();
        failure(t);
    }

    protected abstract void failure(Throwable t);
    protected abstract void success(T result);

    private void restoreContext() {
        AppContext.setRequestId(requestId);
        MDC.put(REQUEST_ID, AppContext.getRequestId());
    }

}
