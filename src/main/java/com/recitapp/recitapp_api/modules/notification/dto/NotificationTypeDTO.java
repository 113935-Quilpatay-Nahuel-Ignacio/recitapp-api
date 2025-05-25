package com.recitapp.recitapp_api.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypeDTO {
    private Long id;
    private String name;
    private String description;
    private String template;
}