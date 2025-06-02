package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.payload.request.DoctorScheduleRequest;
import com.fpt.hivtreatment.payload.response.DoctorScheduleResponse;
import com.fpt.hivtreatment.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DoctorScheduleResponse> createSchedule(@Valid @RequestBody DoctorScheduleRequest request) {
        return new ResponseEntity<>(doctorScheduleService.createSchedule(request), HttpStatus.CREATED);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorScheduleResponse>> getSchedulesByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorScheduleService.getSchedulesByDoctor(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<List<DoctorScheduleResponse>> getSchedulesByDoctorAndDate(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorScheduleService.getSchedulesByDoctorAndDate(doctorId, date));
    }

    @GetMapping("/future")
    public ResponseEntity<List<DoctorScheduleResponse>> getFutureSchedules() {
        return ResponseEntity.ok(doctorScheduleService.getFutureSchedules());
    }
}