package com.campus.seat.controller;

import com.campus.seat.dto.RegistrationRequest;
import com.campus.seat.entity.Registration;
import com.campus.seat.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/activities/{activityId}/register")
    public ResponseEntity<Registration> register(@PathVariable Long activityId,
                                                  @RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(registrationService.register(activityId, request));
    }

    @GetMapping("/activities/{activityId}/registrations")
    public ResponseEntity<List<Registration>> getRegistrations(@PathVariable Long activityId) {
        return ResponseEntity.ok(registrationService.getRegistrations(activityId));
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        registrationService.cancelRegistration(id);
        return ResponseEntity.ok().build();
    }
}
