package com.example.Kafka;

import com.example.DTO.NotificationDTO;
import com.example.Event.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationProducer {
    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public KafkaNotificationProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationEvent notificationEvent) {

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(notificationEvent.getUserId()),
                notificationEvent
        );

        System.out.println(
                "Notification sent to Kafka for user: "
                        + notificationEvent.getUserId()
        );
    }
}
