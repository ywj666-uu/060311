package com.campus.seat.service;

import com.campus.seat.dto.RegistrationRequest;
import com.campus.seat.entity.Registration;
import com.campus.seat.entity.Team;
import com.campus.seat.entity.User;
import com.campus.seat.repository.RegistrationRepository;
import com.campus.seat.repository.TeamRepository;
import com.campus.seat.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               UserRepository userRepository,
                               TeamRepository teamRepository) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Registration register(Long activityId, RegistrationRequest request) {
        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setStudentId(request.getStudentId());
                    newUser.setName(request.getStudentName());
                    return userRepository.save(newUser);
                });

        if (registrationRepository.existsByUserIdAndActivityId(user.getId(), activityId)) {
            throw new RuntimeException("该学生已报名此活动");
        }

        Long teamId = null;
        if (request.getTeamName() != null && !request.getTeamName().trim().isEmpty()) {
            Team team = teamRepository.findByNameAndActivityId(request.getTeamName().trim(), activityId)
                    .orElseGet(() -> {
                        Team newTeam = new Team();
                        newTeam.setName(request.getTeamName().trim());
                        newTeam.setActivityId(activityId);
                        return teamRepository.save(newTeam);
                    });
            teamId = team.getId();
        }

        Registration registration = new Registration();
        registration.setUserId(user.getId());
        registration.setActivityId(activityId);
        registration.setTeamId(teamId);
        registration.setPreferredArea(request.getPreferredArea());
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus("PENDING");

        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrations(Long activityId) {
        return registrationRepository.findByActivityIdOrderByRegistrationTimeAsc(activityId);
    }

    @Transactional
    public void cancelRegistration(Long registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));
        reg.setStatus("CANCELLED");
        reg.setAllocatedSeatId(null);
        registrationRepository.save(reg);
    }
}
