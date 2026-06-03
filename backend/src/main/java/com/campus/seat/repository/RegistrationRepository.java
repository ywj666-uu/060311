package com.campus.seat.repository;

import com.campus.seat.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByActivityIdAndStatusOrderByRegistrationTimeAsc(Long activityId, String status);
    List<Registration> findByActivityIdOrderByRegistrationTimeAsc(Long activityId);
    List<Registration> findByActivityIdAndTeamId(Long activityId, Long teamId);
    boolean existsByUserIdAndActivityId(Long userId, Long activityId);
}
