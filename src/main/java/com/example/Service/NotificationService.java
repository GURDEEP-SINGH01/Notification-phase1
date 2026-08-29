package com.example.Service;

import com.example.DTO.NotificationDTO;
import com.example.Entity.Notification;
import com.example.Repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        Notification notification = new Notification();

        notification.setUserId(notificationDTO.getUserId());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setRead(notificationDTO.isRead());

        Notification saved = notificationRepository.save(notification);

        NotificationDTO response = new NotificationDTO();

        response.setId(saved.getId());
        response.setUserId(saved.getUserId());
        response.setMessage(saved.getMessage());
        response.setType(saved.getType());
        response.setRead(saved.isRead());
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }

    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();

                    dto.setId(notification.getId());
                    dto.setUserId(notification.getUserId());
                    dto.setMessage(notification.getMessage());
                    dto.setType(notification.getType());
                    dto.setRead(notification.isRead());
                    dto.setCreatedAt(notification.getCreatedAt());

                    return dto;
                })
                .toList();
    }
}