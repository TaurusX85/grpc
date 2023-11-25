package com.taurusx85.grpc.client.observer;

import com.taurusx85.grpc.user.DeletedUsers;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeletedUsersStreamObserver implements StreamObserver<DeletedUsers> {

    private final CompletableFuture<List<Integer>> future;
    private final List<Integer> deletedUsers;

    public DeletedUsersStreamObserver(CompletableFuture<List<Integer>> future) {
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
