package com.taurusx85.grpc.server.aop;

import com.google.rpc.Code;
import com.taurusx85.grpc.server.exception.EntityNotFoundException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcErrorAdvice {


    @GrpcExceptionHandler
    public Status handleEntityNotFoundException(EntityNotFoundException e) {
        return Status.fromCode(Status.Code.NOT_FOUND)                           // io.grpc.Status  &  io.grpc.Status.Code
                     .withDescription(e.getMessage());
    }
}