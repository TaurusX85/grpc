package com.taurusx85.grpc.server.service;

import com.google.protobuf.Empty;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.taurusx85.grpc.server.dao.UserDAO;
import com.taurusx85.grpc.server.entity.User;
import com.taurusx85.grpc.server.exception.AlreadyExistsException;
import com.taurusx85.grpc.server.exception.EntityNotFoundException;
import com.taurusx85.grpc.server.observer.CreateUserStreamObserver;
import com.taurusx85.grpc.server.observer.DeletedUsersStreamObserver;
import com.taurusx85.grpc.user.DeletedUsers;
import com.taurusx85.grpc.user.UserId;
import com.taurusx85.grpc.user.UserInput;
import com.taurusx85.grpc.user.UserMessage;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceImplBase;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class UserService extends UserServiceImplBase {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void create(UserInput request, StreamObserver<UserId> responseObserver) {
        log.info("Server - create user");
        UserId response;
        try {
            int id = userDAO.create(request.getName());
            responseObserver.onNext(UserId.newBuilder()
                                          .setId(id)
                                          .build());
            responseObserver.onCompleted();
            log.info("Server - user ID: " + id);
        } catch (AlreadyExistsException e) {
            Status status = Status.newBuilder()
                                  .setCode(Code.ALREADY_EXISTS.getNumber())
                                  .setMessage("User with name: " + request.getName() + " already exists")
                                  .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    @Override
    public void getById(UserId request, StreamObserver<UserMessage> responseObserver) {
        responseObserver.onNext(userDAO.getById(request.getId())
                                       .map(this::toUser)
                                       .orElseThrow(() -> new EntityNotFoundException("User with id: " + request.getId() + " not exists")));
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UserInput> createMultiple(StreamObserver<UserId> responseObserver) {
        return new CreateUserStreamObserver(responseObserver, userDAO);
    }

    @Override
    public void getAll(Empty request, StreamObserver<UserMessage> responseObserver) {
        try {
            for (User user : userDAO.getAll()) {
                responseObserver.onNext(toUser(user));
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UserId> deleteMultiple(StreamObserver<DeletedUsers> responseObserver) {
        return new DeletedUsersStreamObserver(responseObserver, userDAO);
    }


    private UserMessage toUser(User foundUser) {
        return UserMessage.newBuilder()
                          .setId(foundUser.getId())
                          .setName(foundUser.getName())
                          .build();
    }

}
