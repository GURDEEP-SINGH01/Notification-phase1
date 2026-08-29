package com.example.Controller;

import com.example.DTO.NotificationDTO;
import com.example.Entity.Notification;
import com.example.Service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public NotificationDTO createNotification(
            @Valid @RequestBody NotificationDTO notificationDTO) {
        return notificationService.createNotification(notificationDTO);
    }

    @GetMapping("/{userId}")
    public Page<NotificationDTO> getNotifications(
            @PathVariable Long userId,
            Pageable pageable) {

        return notificationService.getNotifications(userId, pageable);
    }

    @GetMapping("/id/{id}")
    public NotificationDTO getNotification(
            @PathVariable Long id) {

        return notificationService.getNotification(id);
    }

    @PutMapping("/{id}")
    public NotificationDTO updateNotification(
            @PathVariable Long id,
            @RequestBody NotificationDTO notificationDTO) {

        return notificationService.updateNotification(id, notificationDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
         notificationService.deleteNotification(id);
    }
}
