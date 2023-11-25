package com.taurusx85.grpc.client.observer;

import com.taurusx85.grpc.user.UserId;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  Receive created user ids from server
 */
@Slf4j
public class CreatedUserIdStreamObserver implements StreamObserver<UserId> {

    private CompletableFuture<List<Integer>> future;
    private List<Integer> createdUserIdList;

    public CreatedUserIdStreamObserver(CompletableFuture<List<Integer>> future) {
        this.future = future;
        this.createdUserIdList = new ArrayList<>();
    }

    /**
     * Add each incoming user id to {@link #createdUserIdList}
     */
    @Override
    public void onNext(UserId value) {
        createdUserIdList.add(value.getId());
        log.info("User created: " + value.getId());
    }

    @Override
    public void onError(Throwable t) {
        future.completeExceptionally(t);
        log.error("Exception: " + t);
    }

    /**
     * Called when server finished to create all users
     */
    @Override
    public void onCompleted() {
        future.complete(createdUserIdList);
        log.info("Completed");
    }
}
