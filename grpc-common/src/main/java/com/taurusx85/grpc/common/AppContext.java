package com.taurusx85.grpc.common;

public class AppContext {

    private static ThreadLocal<String> requestId = new ThreadLocal<>();


    public static String getRequestId() {
        return requestId.get();
    }

    public static void setRequestId(String requestId) {
        AppContext.requestId.set(requestId);
    }

    public static void clean() {
        requestId.remove();
    }
}
