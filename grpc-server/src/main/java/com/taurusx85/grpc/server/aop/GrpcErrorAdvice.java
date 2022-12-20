package com.taurusx85.grpc.server.aop;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.taurusx85.grpc.server.exception.EntityNotFoundException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcErrorAdvice {


    @GrpcExceptionHandler
    public Status handleEntityNotFoundException(EntityNotFoundException e) {
        return Status.newBuilder()
                     .setCode(Code.NOT_FOUND.getNumber())
                     .setMessage(e.getMessage())
                     .build();
    }
}
