package com.example.Service;

import com.example.DTO.NotificationDTO;
import com.example.Entity.Notification;
import com.example.Event.NotificationEvent;
import com.example.Exception.NotificationNotFoundException;
import com.example.Kafka.KafkaNotificationProducer;
import com.example.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final KafkaNotificationProducer kafkaNotificationProducer;

    @Value("${notification.ttl.minutes}")
    private long ttlMinutes;

    public NotificationService(NotificationRepository notificationRepository,KafkaNotificationProducer kafkaNotificationProducer) {
        this.notificationRepository = notificationRepository;
        this.kafkaNotificationProducer=kafkaNotificationProducer;
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO) throws NotificationNotFoundException {
        Notification notification = new Notification();

        notification.setUserId(notificationDTO.getUserId());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setRead(notificationDTO.isRead());
        notification.setExpiresAt( LocalDateTime.now().plusMinutes(ttlMinutes));

        Notification saved = notificationRepository.save(notification);

        NotificationDTO response = new NotificationDTO();

        response.setId(saved.getId());
        response.setUserId(saved.getUserId());
        response.setMessage(saved.getMessage());
        response.setType(saved.getType());
        response.setRead(saved.isRead());
        response.setCreatedAt(saved.getCreatedAt());
        response.setExpiresAt(saved.getExpiresAt());

        // Send the saved notification to Kafka
        // convert notification dto to Notification (optimization)
        NotificationEvent event = new NotificationEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getMessage(),
                saved.getType()
        );
        kafkaNotificationProducer.sendNotification(event);

        return response;
    }

    public Page<NotificationDTO> getNotifications(
            Long userId,
            Pageable pageable) {
        return notificationRepository
                .findByUserId(userId, pageable)
                .map(notification -> {

                    NotificationDTO dto = new NotificationDTO();

                    dto.setId(notification.getId());
                    dto.setUserId(notification.getUserId());
                    dto.setMessage(notification.getMessage());
                    dto.setType(notification.getType());
                    dto.setRead(notification.isRead());
                    dto.setCreatedAt(notification.getCreatedAt());
                    dto.setExpiresAt(notification.getExpiresAt());

                    return dto;
                });
    }

    public NotificationDTO getNotification(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id
                        ));

        NotificationDTO dto = new NotificationDTO();

        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setExpiresAt(notification.getExpiresAt());

        return dto;
    }

    public NotificationDTO updateNotification(
            Long id,
            NotificationDTO notificationDTO) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));

        notification.setUserId(notificationDTO.getUserId());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setRead(notificationDTO.isRead());

        Notification updated = notificationRepository.save(notification);

        NotificationDTO response = new NotificationDTO();

        response.setId(updated.getId());
        response.setUserId(updated.getUserId());
        response.setMessage(updated.getMessage());
        response.setType(updated.getType());
        response.setRead(updated.isRead());
        response.setCreatedAt(updated.getCreatedAt());

        return response;
    }

    public void deleteNotification(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));

        notificationRepository.delete(notification);
    }
}