package com.example.Scheduler;

import com.example.Entity.Notification;
import com.example.Repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationCleanupScheduler {

    private NotificationRepository notificationRepository;

    public NotificationCleanupScheduler(NotificationRepository notificationRepository) {
        this.notificationRepository=notificationRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanUpExpiredNotifications(){
        LocalDateTime now= LocalDateTime.now();

        List<Notification> expiredNotifications= notificationRepository.findByExpiresAtBefore(now);

        notificationRepository.deleteAll(expiredNotifications);

        System.out.println(
                "Deleted " + expiredNotifications.size()
                        + " expired notifications"
        );
    }

}
