package com.example.Kafka;

import com.example.DTO.NotificationDTO;
import com.example.Event.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationConsumer {

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group"
    )
    public void consume(NotificationEvent notificationEvent) {

        System.out.println(
                "Received notification from Kafka for user: "
                        + notificationEvent.getUserId()
        );

        System.out.println(
                "Message: " + notificationEvent.getMessage()
        );
    }
}
