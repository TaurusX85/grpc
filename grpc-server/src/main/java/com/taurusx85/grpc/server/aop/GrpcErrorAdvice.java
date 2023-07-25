package com.taurusx85.grpc.server.aop;

import com.google.rpc.Code;
import com.taurusx85.grpc.server.exception.AlreadyExistsException;
import com.taurusx85.grpc.server.exception.EntityNotFoundException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * Catch exceptions thrown while processing gRPC calls
 * and convert them to corresponding response Status
 * to send to client
 */
@GrpcAdvice
public class GrpcErrorAdvice {


    @GrpcExceptionHandler
    public Status handleEntityNotFoundException(EntityNotFoundException e) {
        return Status.fromCode(Status.Code.NOT_FOUND)                           // io.grpc.Status  &  io.grpc.Status.Code
                     .withDescription(e.getMessage());
    }

    @GrpcExceptionHandler
    public Status handleRuntimeException(AlreadyExistsException e) {
        return Status.fromCode(Status.Code.ALREADY_EXISTS)
                     .withDescription(e.getMessage());
    }

    @GrpcExceptionHandler
    public Status handleRuntimeException(RuntimeException e) {
        return Status.fromCode(Status.Code.INTERNAL)
                     .withDescription(e.getMessage());
    }
}
