package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryDTO {
    private Long userId;
    private Long totalNotifications;
    private Long unreadNotifications;
    private LocalDateTime lastNotificationDate;
    private List<NotificationDTO> recentNotifications;
}