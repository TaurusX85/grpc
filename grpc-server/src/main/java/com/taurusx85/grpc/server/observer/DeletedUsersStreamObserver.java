package com.taurusx85.grpc.server.observer;

import com.taurusx85.grpc.server.dao.UserDAO;
import com.taurusx85.grpc.user.DeletedUsers;
import com.taurusx85.grpc.user.UserId;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeletedUsersStreamObserver implements StreamObserver<UserId> {

    private final List<Integer> removedUsersIdList = new ArrayList<>();
    private final StreamObserver<DeletedUsers> responseObserver;
    private final UserDAO userDAO;

    public DeletedUsersStreamObserver(StreamObserver<DeletedUsers> responseObserver, UserDAO userDAO) {
        this.responseObserver = responseObserver;
        this.userDAO = userDAO;
    }

    @Override
    public void onNext(UserId userId) {
        try {
            boolean removed = userDAO.removeById(userId.getId());
            if (removed)
                removedUsersIdList.add(userId.getId());
        } catch (Exception e) {
            log.error(e.toString());
            responseObserver.onError(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        // exception on client side - not required
        // connection broken ??
    }

    @Override
    public void onCompleted() {
        responseObserver.onNext(DeletedUsers.newBuilder()
                                            .addAllIds(removedUsersIdList)
                                            .build());
        responseObserver.onCompleted();
    }
}
