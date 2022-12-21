package com.taurusx85.grpc.client.exception;

public class StreamExecutionException extends RuntimeException {

    public StreamExecutionException(String message) {
        super(message);
    }

    public StreamExecutionException(Exception e) {
        super(e);
    }
}
