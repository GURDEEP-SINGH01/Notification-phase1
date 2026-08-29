package com.example.Controller;

import com.example.DTO.NotificationDTO;
import com.example.Entity.Notification;
import com.example.Service.NotificationService;
import jakarta.validation.Valid;
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
    public List<NotificationDTO> getNotifications(
            @PathVariable Long userId) {

        return notificationService.getNotifications(userId);
    }
}
