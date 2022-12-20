package com.taurusx85.grpc.server.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.taurusx85.grpc.server.entity.User;
import com.taurusx85.grpc.server.exception.EntityNotFoundException;
import com.taurusx85.grpc.user.*;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceImplBase;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

@Slf4j
@GrpcService
public class UserService extends UserServiceImplBase {

    private List<User> users = new ArrayList<>();

    @Override
    public void create(UserInput request, StreamObserver<UserId> responseObserver) {
        log.info("Server - create user");
        UserId response;
        try {
            int id = createUser(request.getName());
            responseObserver.onNext(UserId.newBuilder()
                                          .setId(id)
                                          .build());
            responseObserver.onCompleted();
            log.info("Server - user ID: " + id);
        } catch (Exception e) {
            Status status = Status.newBuilder()
                                  .setCode(Code.ALREADY_EXISTS.getNumber())
                                  .setMessage("User with name: " + request.getName() + " already exists")
                                  .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }

    }

    @Override
    public void getById(UserId request, StreamObserver<UserMessage> responseObserver) {
        responseObserver.onNext(users.stream()
                                     .filter(user -> user.getId() == request.getId())
                                     .map(this::toUser)
                                     .findFirst()
                                     .orElseThrow(() -> new EntityNotFoundException("User with id: " + request.getId() + " not exists")));
        responseObserver.onCompleted();
    }


//    ====================== PRIVATE ==================

    private int createUser(String name) {
        if (users.stream().anyMatch(user -> user.getName().equals(name)))
            throw new RuntimeException("Already exists");

        User user = new User();
        user.setId(new Random().nextInt(Integer.MAX_VALUE));
        user.setName(name);
        users.add(user);
        return user.getId();
    }

    private UserMessage toUser(User foundUser) {
        return UserMessage.newBuilder()
                          .setId(foundUser.getId())
                          .setName(foundUser.getName())
                          .build();
    }

}
