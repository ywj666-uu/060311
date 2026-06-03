package com.campus.seat.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String studentId;
    private String studentName;
    private String preferredArea;
    private String teamName;
}
