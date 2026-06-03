package com.campus.seat.controller;

import com.campus.seat.dto.ActivityCreateRequest;
import com.campus.seat.entity.Activity;
import com.campus.seat.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> create(@RequestBody ActivityCreateRequest request) {
        return ResponseEntity.ok(activityService.createActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<Activity>> list() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> get(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Activity> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(activityService.updateStatus(id, body.get("status")));
    }
}
