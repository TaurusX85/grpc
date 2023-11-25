package com.taurusx85.grpc.client.observer;

import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.user.UserMessage;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  Receive all users from server
 */
public class GetAllUsersStreamObserver implements StreamObserver<UserMessage> {

    private final CompletableFuture<List<UserDTO>> future;
    private final List<UserDTO> users;


    public GetAllUsersStreamObserver(CompletableFuture<List<UserDTO>> allUsersResponse) {
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
