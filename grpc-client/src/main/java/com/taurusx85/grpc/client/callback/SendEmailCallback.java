package com.taurusx85.grpc.client.callback;

import com.google.common.util.concurrent.FutureCallback;
import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.user.UserMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SendEmailCallback implements FutureCallback<UserMessage> {

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
