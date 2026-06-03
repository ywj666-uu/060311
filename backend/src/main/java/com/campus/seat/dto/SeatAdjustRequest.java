package com.campus.seat.dto;

import lombok.Data;

@Data
public class SeatAdjustRequest {
    private Long registrationId;
    private Long newSeatId;
}
