package com.campus.seat.controller;

import com.campus.seat.dto.SeatMapResponse;
import com.campus.seat.service.ExcelExportService;
import com.campus.seat.service.SeatAllocationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExcelExportService excelExportService;
    private final SeatAllocationService seatAllocationService;

    public ExportController(ExcelExportService excelExportService,
                            SeatAllocationService seatAllocationService) {
        this.excelExportService = excelExportService;
        this.seatAllocationService = seatAllocationService;
    }

    @GetMapping("/activities/{activityId}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long activityId) throws Exception {
        SeatMapResponse seatMap = seatAllocationService.getSeatMap(activityId);
        byte[] data = excelExportService.exportSeatMap(seatMap);

        String filename = URLEncoder.encode("座位表_" + seatMap.getActivityTitle() + ".xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
