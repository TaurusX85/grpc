package com.taurusx85.grpc.server.observer;

import com.taurusx85.grpc.server.dao.UserDAO;
import com.taurusx85.grpc.server.exception.AlreadyExistsException;
import com.taurusx85.grpc.user.UserId;
import com.taurusx85.grpc.user.UserInput;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.common.ExpressionUtils;

@Slf4j
public class CreateUserStreamObserver implements StreamObserver<UserInput> {

    private final StreamObserver<UserId> responseObserver;
    private final UserDAO userDAO;

    public CreateUserStreamObserver(StreamObserver<UserId> responseObserver, UserDAO userDAO) {
        this.responseObserver = responseObserver;
        this.userDAO = userDAO;
    }

    @Override
    public void onNext(UserInput value) {
        try {
            int id = userDAO.create(value.getName());
            responseObserver.onNext(UserId.newBuilder()
                                          .setId(id)
                                          .build());
            log.info("User created: " + id);
        } catch (AlreadyExistsException e) {
            log.error(e.toString());
            responseObserver.onError(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        responseObserver.onError(t);
        log.error("Exception: " + t);
    }

    @Override
    public void onCompleted() {
        responseObserver.onCompleted();
        log.info("Completed");
    }
}
