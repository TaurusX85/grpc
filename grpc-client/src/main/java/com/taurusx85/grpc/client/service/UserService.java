package com.taurusx85.grpc.client.service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.taurusx85.grpc.client.config.GrpcChannelManager;
import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.client.exception.AlreadyExistsException;
import com.taurusx85.grpc.user.UserId;
import com.taurusx85.grpc.user.UserInput;
import com.taurusx85.grpc.user.UserMessage;
import com.taurusx85.grpc.user.UserServiceGrpc;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceFutureStub;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceBlockingStub;
import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.rpc.Code.forNumber;
import static com.taurusx85.grpc.user.UserServiceGrpc.newFutureStub;

@Slf4j
@Service
public class UserService {

    @GrpcClient("local-server")
    private UserServiceBlockingStub blockingStub;
    private UserServiceFutureStub futureStub;

    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public UserService(GrpcChannelManager channelManager) {
        futureStub = newFutureStub(channelManager.getServerChannel());
    }


    public Integer create(String name) {
        log.info("Client - create user");
        return blockingStub.create(UserInput.newBuilder()
                                            .setName(name)
                                            .build())
                           .getId();
    }


    public UserDTO getById(Integer id) {
        UserMessage response = blockingStub.getById(UserId.newBuilder()
                                                      .setId(id)
                                                      .build());
        return new UserDTO(response.getId(), response.getName());
    }


    public void notifyUser(Integer userId, NotificationInput input) {
        Context newContext = Context.current().fork();
        Context origContext = newContext.attach();
        try {
            ListenableFuture<UserMessage> response = futureStub.getById(UserId.newBuilder()
                                                                              .setId(userId)
                                                                              .build());
            Futures.addCallback(response, new SendEmailCallback(input), executorService);
        } finally {
            newContext.detach(origContext);
        }
    }



    @AllArgsConstructor
    private static class SendEmailCallback implements FutureCallback<UserMessage> {

        private NotificationInput notificationInput;

        @Override
        public void onSuccess(UserMessage userMessage) {
            log.info("Sending email to: " + userMessage.getName() + "; Message: " + notificationInput.getMessage());
        }

        @Override
        public void onFailure(Throwable t) {
            log.error("Error fetching user: " + t.getMessage());
        }
    }
}
