package com.taurusx85.grpc.client.aop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.rpc.Code.forNumber;
import static com.taurusx85.grpc.common.AppContext.getRequestId;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

    private final Map<Code, HttpStatus> codeToHttpStatusMap = Map.of(Code.ALREADY_EXISTS, HttpStatus.CONFLICT,
                                                                     Code.NOT_FOUND,      HttpStatus.NOT_FOUND);


    @ExceptionHandler
    ResponseEntity<ApiError> handleCommonException(Exception e) {
        ApiError apiError = new ApiError(e.getMessage(), getRequestId());
        return new ResponseEntity<>(apiError, INTERNAL_SERVER_ERROR);
    }


    /**
     * Catch StatusRuntimeException from call to gRPC client,
     * extract response status and convert it to proper HttpCode
     *
     * @param e - Exception thrown from gRPC call
     * @return ApiError
     */
    @ExceptionHandler
    ResponseEntity<ApiError> handleStatusRuntimeException(StatusRuntimeException e) {
        log.error(e.toString());
        ApiError apiError;
        Status status = StatusProto.fromThrowable(e);
        if (status == null) {
            apiError = new ApiError(e.getMessage(), getRequestId());
            return new ResponseEntity<>(apiError, INTERNAL_SERVER_ERROR);
        }

        Code responseCode = forNumber(status.getCode());
        HttpStatus httpStatus = codeToHttpStatusMap.getOrDefault(responseCode, HttpStatus.INTERNAL_SERVER_ERROR);
        apiError = new ApiError(status.getMessage(), getRequestId());
        return new ResponseEntity<>(apiError, httpStatus);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class ApiError {
        @JsonInclude(NON_NULL)
        private String message;
        private String requestId;
    }

}
