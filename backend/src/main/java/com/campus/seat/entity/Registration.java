package com.campus.seat.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long activityId;

    private Long teamId;

    private String preferredArea;

    private Long allocatedSeatId;

    private LocalDateTime registrationTime = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "PENDING";
}
