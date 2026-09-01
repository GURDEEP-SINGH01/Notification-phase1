package com.example.DTO;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class NotificationDTO {

    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String message;

    @NotBlank
    private String type;

    private boolean read;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}
