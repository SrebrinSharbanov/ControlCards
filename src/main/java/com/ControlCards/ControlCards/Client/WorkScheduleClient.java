package com.ControlCards.ControlCards.Client;

import com.ControlCards.ControlCards.DTO.WorkScheduleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "workScheduleService", url = "${workschedule.service.url}")
public interface WorkScheduleClient {
    
    @GetMapping("/api/schedules")
    List<WorkScheduleDTO> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer shift,
            @RequestParam(required = false) String workCenter
    );
    
    @GetMapping("/api/schedules/{id}")
    WorkScheduleDTO getScheduleById(@PathVariable UUID id);
    
    @PostMapping("/api/schedules")
    WorkScheduleDTO createSchedule(@RequestBody WorkScheduleDTO dto);
    
    @PutMapping("/api/schedules/{id}")
    WorkScheduleDTO updateSchedule(@PathVariable UUID id, @RequestBody WorkScheduleDTO dto);
    
    @DeleteMapping("/api/schedules/{id}")
    void deleteSchedule(@PathVariable UUID id);
}

