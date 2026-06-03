package com.campus.seat.controller;

import com.campus.seat.entity.Venue;
import com.campus.seat.entity.Seat;
import com.campus.seat.repository.VenueRepository;
import com.campus.seat.repository.SeatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    public VenueController(VenueRepository venueRepository, SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping
    public ResponseEntity<List<Venue>> list() {
        return ResponseEntity.ok(venueRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venue> get(@PathVariable Long id) {
        return ResponseEntity.ok(venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场地不存在")));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Seat>> getSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatRepository.findByVenueIdOrderByRowNumAscColNumAsc(id));
    }
}
