package com.taurusx85.grpc.client.service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import com.taurusx85.grpc.client.config.GrpcChannelManager;
import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.client.dto.input.UserCreationInput;
import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.user.*;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceBlockingStub;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceFutureStub;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceImplBase;
import com.taurusx85.grpc.user.UserServiceGrpc.UserServiceStub;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class UserService extends UserServiceImplBase  {

    @GrpcClient("local-server")
    private UserServiceBlockingStub blockingStub;
    private UserServiceFutureStub futureStub;
    private UserServiceStub streamingStub;

    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public UserService(GrpcChannelManager channelManager) {
        this.futureStub = UserServiceGrpc.newFutureStub(channelManager.getServerChannel());
        this.streamingStub = UserServiceGrpc.newStub(channelManager.getServerChannel());
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
        }
        stream.onCompleted();

        try {
            List<Integer> deletedUsersIdList = future.get();
            log.info(deletedUsersIdList.toString());
        } catch (InterruptedException | ExecutionException e) {
            throw e.getCause();
        }
    }


    //    ==================================== PRIVATE =================================
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


    /**
     *  Receive all users from server
     */
    private static class GetAllUsersStreamObserver implements StreamObserver<UserMessage> {

        private CompletableFuture<List<UserDTO>> future;
        private List<UserDTO> users;

        GetAllUsersStreamObserver(CompletableFuture<List<UserDTO>> allUsersResponse) {
            this.future = allUsersResponse;
            this.users = new ArrayList<>();
        }

        /**
         * <p> Called when received a next user from server
         * <p> Add each incoming user id to {@link #users} list
         */
        @Override
        public void onNext(UserMessage value) {
            users.add(new UserDTO(value.getId(), value.getName()));
        }

        @Override
        public void onError(Throwable t) {
            future.completeExceptionally(t);
        }

        /**
         * Called when server finished to send users. After last sent
         */
        @Override
        public void onCompleted() {
            future.complete(users);
        }
    }

    private static class DeletedUsersStreamObserver implements StreamObserver<DeletedUsers> {

        private final CompletableFuture<List<Integer>> future;
        private List<Integer> deletedUsers;

        DeletedUsersStreamObserver(CompletableFuture<List<Integer>> future) {
            this.future = future;
            this.deletedUsers = new ArrayList<>();
        }

        @Override
        public void onNext(DeletedUsers value) {
            deletedUsers.addAll(value.getIdsList());
        }

        @Override
        public void onError(Throwable t) {
            future.completeExceptionally(t);
        }

        @Override
        public void onCompleted() {
            future.complete(deletedUsers);
        }
    }


    /**
     *  Receive created user ids from server
     */
    private static class CreatedUserIdStreamObserver implements StreamObserver<UserId> {

        private CompletableFuture<List<Integer>> future;
        private List<Integer> createdUserIdList;

        CreatedUserIdStreamObserver(CompletableFuture<List<Integer>> future) {
            this.future = future;
            this.createdUserIdList = new ArrayList<>();
        }

        /**
         * Add each incoming user id to {@link #createdUserIdList}
         */
        @Override
        public void onNext(UserId value) {
            createdUserIdList.add(value.getId());
        }

        @Override
        public void onError(Throwable t) {
            future.completeExceptionally(t);
        }

        /**
         * Called when server finished to create all users
         */
        @Override
        public void onCompleted() {
            future.complete(createdUserIdList);
        }
    }
}
