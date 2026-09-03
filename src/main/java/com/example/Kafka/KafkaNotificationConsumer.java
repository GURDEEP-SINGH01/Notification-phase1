package com.example.Kafka;

import com.example.DTO.NotificationDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationConsumer {

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group"
    )
    public void consume(NotificationDTO notification) {

        System.out.println(
                "Received notification from Kafka for user: "
                        + notification.getUserId()
        );

        System.out.println(
                "Message: " + notification.getMessage()
        );
    }
}
