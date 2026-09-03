package com.example.Kafka;

import com.example.DTO.NotificationDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationProducer {
    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    public KafkaNotificationProducer(KafkaTemplate<String, NotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationDTO notificationDTO) {

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(notificationDTO.getUserId()),
                notificationDTO
        );

        System.out.println(
                "Notification sent to Kafka for user: "
                        + notificationDTO.getUserId()
        );
    }
}
