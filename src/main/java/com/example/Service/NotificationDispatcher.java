package com.example.Service;

import com.example.Event.NotificationEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatcher {

    public void dispatch(NotificationEvent event) {

        switch (event.getType()) {

            case "EMAIL":
                sendEmail(event);
                break;

            case "SMS":
                sendSms(event);
                break;

            case "PUSH":
                sendPush(event);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported notification type: "
                                + event.getType()
                );
        }
    }

    private void sendEmail(NotificationEvent event) {
        System.out.println(
                "EMAIL sent to user "
                        + event.getUserId()
                        + ": "
                        + event.getMessage()
        );
    }

    private void sendSms(NotificationEvent event) {
        System.out.println(
                "SMS sent to user "
                        + event.getUserId()
                        + ": "
                        + event.getMessage()
        );
    }

    private void sendPush(NotificationEvent event) {
        System.out.println(
                "PUSH notification sent to user "
                        + event.getUserId()
                        + ": "
                        + event.getMessage()
        );
    }
}
