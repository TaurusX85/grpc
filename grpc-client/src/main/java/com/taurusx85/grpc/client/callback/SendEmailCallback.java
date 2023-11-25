package com.taurusx85.grpc.client.callback;

import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.common.AppContext;
import com.taurusx85.grpc.common.ForwardedContextFutureCallback;
import com.taurusx85.grpc.user.UserMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailCallback extends ForwardedContextFutureCallback<UserMessage> {

    private final NotificationInput notificationInput;

    public SendEmailCallback(NotificationInput notificationInput) {
        super(AppContext.getRequestId());
        this.notificationInput = notificationInput;
    }


    @Override
    protected void success(UserMessage userMessage) {
        log.info("Sending email to: " + userMessage.getName() + "; Message: " + notificationInput.getMessage());
    }

    @Override
    protected void failure(Throwable t) {
        log.error("Error fetching user: " + t.getMessage());
    }

}
