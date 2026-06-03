package com.campus.seat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityCreateRequest {
    private String title;
    private String description;
    private Long venueId;
    private LocalDateTime startTime;
}
