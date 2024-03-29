package com.taurusx85.grpc.client.service;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.taurusx85.grpc.client.callback.SendEmailCallback;
import com.taurusx85.grpc.client.config.GrpcChannelManager;
import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.client.dto.input.UserCreationInput;
import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.client.observer.CreatedUserIdStreamObserver;
import com.taurusx85.grpc.client.observer.DeletedUsersStreamObserver;
import com.taurusx85.grpc.client.observer.GetAllUsersStreamObserver;
import com.taurusx85.grpc.user.*;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceBlockingStub;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceFutureStub;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceImplBase;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceStub;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;


@Slf4j
@Service
public class UserService extends UserServiceImplBase  {

    public static final int ONE_SECOND = 1_000;
    @GrpcClient("target-server")
    private UserServiceBlockingStub blockingStub;
    private UserServiceFutureStub futureStub;
    private UserServiceStub streamingStub;
    private RestTemplate restTemplate;


    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public UserService(GrpcChannelManager channelManager) {
        this.futureStub = UserServiceGrpc.newFutureStub(channelManager.getServerChannel());
        this.streamingStub = UserServiceGrpc.newStub(channelManager.getServerChannel());
        this.restTemplate = new RestTemplate();
    }


    public Integer create(String name) {
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

    @SneakyThrows
    public List<Integer> createMultiple(List<UserCreationInput> input) {
        CompletableFuture<List<Integer>> createdUserIdList = new CompletableFuture<>();
        StreamObserver<UserInput> inputStream = streamingStub.createMultiple(new CreatedUserIdStreamObserver(createdUserIdList));
        for (UserCreationInput userInput : input) {
            inputStream.onNext(UserInput.newBuilder()
                                        .setName(userInput.getName())
                                        .build());
        }
        inputStream.onCompleted();
        try {
            return createdUserIdList.get();
        } catch (InterruptedException | ExecutionException e) {
            throw e.getCause();
        }
    }

    @SneakyThrows
    public List<UserDTO> getAll() {
        CompletableFuture<List<UserDTO>> allUsersResponse = new CompletableFuture<>();
        streamingStub.getAll(Empty.newBuilder().build(), new GetAllUsersStreamObserver(allUsersResponse));
        try {
            return allUsersResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            throw e.getCause();
        }
    }

    @SneakyThrows
    public void deleteMultiple(List<Integer> ids) {
        CompletableFuture<List<Integer>> future = new CompletableFuture<>();

        StreamObserver<UserId> stream = streamingStub.deleteMultiple(new DeletedUsersStreamObserver(future));

        for (Integer userId : ids) {
            stream.onNext(UserId.newBuilder()
                                .setId(userId)
                                .build());
            log.info("Removing user: " + userId);
        }
        stream.onCompleted();
        log.info("All users sent for removal");

        try {
            List<Integer> deletedUsersIdList = future.get();
            log.info("All users removed: " + deletedUsersIdList.toString());
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.toString());
            throw e.getCause();
        }
    }

    public UserDTO getByNameWithDeadline() {
        UserMessage userMessage = blockingStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                                              .getByName(NameInput.newBuilder()
                                                                  .setName(StringValue.of("Tom"))
                                                                  .build());
        return new UserDTO(userMessage.getId(), userMessage.getName());
    }

    @SneakyThrows
    public UserDTO getByNameAndCancel() {
        ListenableFuture<UserMessage> future = futureStub.getByName(NameInput.newBuilder().build());
        Thread.sleep(ONE_SECOND);
        future.cancel(false);
        UserMessage userMessage = future.get();
        log.info("getByNameAndCancel finished");
        return new UserDTO(userMessage.getId(), userMessage.getName());
    }

}
