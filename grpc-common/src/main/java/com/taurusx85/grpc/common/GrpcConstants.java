package com.taurusx85.grpc.common;

import io.grpc.Context;
import io.grpc.Metadata;

public class GrpcConstants {

    public static final String REQUEST_ID = "requestId";
    public static final Metadata.Key<String> REQUEST_ID_HEADER_KEY = Metadata.Key.of(REQUEST_ID, Metadata.ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> REQUEST_CONTEXT  = Context.key(REQUEST_ID);
    public static final int DEADLINE = -1;
    public static final int CANCEL = -2;

}
