package com.campus.seat.service;

import com.campus.seat.dto.ActivityCreateRequest;
import com.campus.seat.entity.Activity;
import com.campus.seat.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity createActivity(ActivityCreateRequest request) {
        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setVenueId(request.getVenueId());
        activity.setStartTime(request.getStartTime());
        activity.setStatus("OPEN");
        activity.setCreatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity getActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
    }

    public Activity updateStatus(Long id, String status) {
        Activity activity = getActivity(id);
        activity.setStatus(status);
        return activityRepository.save(activity);
    }
}
