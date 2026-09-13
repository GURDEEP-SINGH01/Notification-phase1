package com.example.Kafka;

import com.example.Event.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Service
public class KafkaNotificationDltConsumer {

    @KafkaListener(
            topics = "notification-events-dlt",
            groupId = "notification-dlt-group"
    )
    public void consume(NotificationEvent event) {

        System.out.println(
                "========== DLT MESSAGE =========="
        );

        System.out.println(
                "Notification ID: "
                        + event.getNotificationId()
        );

        System.out.println(
                "User ID: "
                        + event.getUserId()
        );

        System.out.println(
                "Message: "
                        + event.getMessage()
        );

        System.out.println(
                "Channel: "
                        + event.getChannel()
        );

        System.out.println(
                "Category: "
                        + event.getCategory()
        );

        System.out.println(
                "================================="
        );
    }
}
