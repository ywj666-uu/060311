package com.campus.seat.controller;

import com.campus.seat.algorithm.SeatAllocator;
import com.campus.seat.dto.SeatAdjustRequest;
import com.campus.seat.dto.SeatMapResponse;
import com.campus.seat.service.SeatAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatAllocationService seatAllocationService;

    public SeatController(SeatAllocationService seatAllocationService) {
        this.seatAllocationService = seatAllocationService;
    }

    @GetMapping("/activities/{activityId}/seat-map")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long activityId) {
        return ResponseEntity.ok(seatAllocationService.getSeatMap(activityId));
    }

    @PostMapping("/activities/{activityId}/allocate")
    public ResponseEntity<Map<String, Object>> allocate(@PathVariable Long activityId) {
        List<SeatAllocator.AllocationResult> results = seatAllocationService.allocateSeats(activityId);
        return ResponseEntity.ok(Map.of(
                "message", "分配完成",
                "allocatedCount", results.size()
        ));
    }

    /**
     * 调整座位，返回最新座位图供前端立即重绘
     */
    @PutMapping("/seats/adjust")
    public ResponseEntity<SeatMapResponse> adjust(@RequestBody SeatAdjustRequest request) {
        SeatMapResponse updated = seatAllocationService.adjustSeat(request.getRegistrationId(), request.getNewSeatId());
        return ResponseEntity.ok(updated);
    }

    /**
     * 交换座位，返回最新座位图供前端立即重绘
     */
    @PutMapping("/seats/swap")
    public ResponseEntity<SeatMapResponse> swap(@RequestBody Map<String, Long> body) {
        SeatMapResponse updated = seatAllocationService.swapSeats(body.get("regId1"), body.get("regId2"));
        return ResponseEntity.ok(updated);
    }
}
