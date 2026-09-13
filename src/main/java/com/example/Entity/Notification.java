package com.example.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String message;

    private String channel;

    private String category;

    private boolean read;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().atZone(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
    }

}