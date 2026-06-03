package com.campus.seat.repository;

import com.campus.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueIdOrderByRowNumAscColNumAsc(Long venueId);
    List<Seat> findByVenueIdAndAreaTag(Long venueId, String areaTag);
}
