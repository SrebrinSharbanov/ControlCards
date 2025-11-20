package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Client.WorkScheduleClient;
import com.ControlCards.ControlCards.DTO.WorkScheduleDTO;
import com.ControlCards.ControlCards.Exception.UserNotFoundException;
import com.ControlCards.ControlCards.Exception.WorkCenterNotFoundException;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final WorkScheduleClient workScheduleClient;
    private final WorkCenterService workCenterService;
    private final LogEntryService logEntryService;
    private final UserService userService;

    @Autowired
    public ScheduleController(WorkScheduleClient workScheduleClient,
                              WorkCenterService workCenterService,
                              LogEntryService logEntryService,
                              UserService userService) {
        this.workScheduleClient = workScheduleClient;
        this.workCenterService = workCenterService;
        this.logEntryService = logEntryService;
        this.userService = userService;
    }

    private void logAction(UserDetails userDetails, String action) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
            logEntryService.createLog(currentUser, action);
            log.info("Schedule action logged: {} by user: {}", action, currentUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to log schedule action: {}", e.getMessage());
        }
    }

    @GetMapping
    public ModelAndView showSchedulesPage() {
        log.debug("Showing schedules page");
        ModelAndView modelAndView = new ModelAndView("schedules");
        modelAndView.addObject("workCenters", workCenterService.findAllActive());
        return modelAndView;
    }

    @GetMapping("/search")
    public ModelAndView searchSchedules(
            @RequestParam(required = false) UUID workCenterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer shift) {
        
        log.info("Searching schedules - workCenterId: {}, date: {}, shift: {}", workCenterId, date, shift);
        
        List<WorkScheduleDTO> schedules;
        WorkCenter selectedWorkCenter = null;
        String workCenterNumber = null;
        
        if (workCenterId != null) {
            selectedWorkCenter = workCenterService.findById(workCenterId)
                    .orElseThrow(() -> new WorkCenterNotFoundException("Work center not found"));
            workCenterNumber = selectedWorkCenter.getNumber();
        }
        
        final String finalWorkCenterNumber = workCenterNumber;
        final Integer finalShift = shift;
        
        schedules = workScheduleClient.getSchedules(date, shift, workCenterNumber);
        
        if (shift != null && date == null) {
            schedules = schedules.stream()
                    .filter(s -> s.getShift() != null && s.getShift().equals(finalShift))
                    .collect(Collectors.toList());
            log.debug("Filtered schedules by shift {}: {} results", shift, schedules.size());
        }
        
        if (finalWorkCenterNumber != null && date == null && shift == null) {
            schedules = schedules.stream()
                    .filter(s -> finalWorkCenterNumber.equals(s.getWorkCenter()))
                    .collect(Collectors.toList());
            log.debug("Filtered schedules by workCenter {}: {} results", finalWorkCenterNumber, schedules.size());
        }
        
        if (finalWorkCenterNumber != null && shift != null && date == null) {
            schedules = schedules.stream()
                    .filter(s -> finalWorkCenterNumber.equals(s.getWorkCenter()) 
                            && s.getShift() != null && s.getShift().equals(finalShift))
                    .collect(Collectors.toList());
            log.debug("Filtered schedules by workCenter {} and shift {}: {} results", 
                    finalWorkCenterNumber, shift, schedules.size());
        }
        
        ModelAndView modelAndView = new ModelAndView("schedules");
        modelAndView.addObject("workCenters", workCenterService.findAllActive());
        modelAndView.addObject("schedules", schedules);
        modelAndView.addObject("selectedWorkCenter", selectedWorkCenter);
        modelAndView.addObject("selectedDate", date);
        modelAndView.addObject("selectedShift", shift);
        return modelAndView;
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public ModelAndView showCreateScheduleForm(@RequestParam(required = false) String error) {
        log.debug("Showing create schedule form");
        ModelAndView modelAndView = new ModelAndView("schedule-form");
        modelAndView.addObject("workCenters", workCenterService.findAllActive());
        modelAndView.addObject("schedule", new WorkScheduleDTO());
        modelAndView.addObject("isEdit", false);
        if (error != null && !error.isEmpty()) {
            modelAndView.addObject("error", error);
        }
        return modelAndView;
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public ModelAndView createSchedule(@ModelAttribute WorkScheduleDTO scheduleDTO,
                                      @RequestParam(required = false) String error,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating new schedule via Feign Client - production order: {}, shift: {}", 
                scheduleDTO.getProductionOrder(), scheduleDTO.getShift());
        
        if (scheduleDTO.getWorkCenterId() == null) {
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", false);
            modelAndView.addObject("error", "Моля, изберете работен център");
            return modelAndView;
        }
        
        WorkCenter selectedWorkCenter = workCenterService.findById(scheduleDTO.getWorkCenterId())
                .orElseThrow(() -> new WorkCenterNotFoundException("Work center not found: " + scheduleDTO.getWorkCenterId()));
        
        scheduleDTO.setWorkCenter(selectedWorkCenter.getNumber());
        
        try {
            log.debug("Schedule DTO before sending: date={}, shift={}, workCenter={}, productionOrder={}", 
                    scheduleDTO.getDate(), scheduleDTO.getShift(), scheduleDTO.getWorkCenter(), scheduleDTO.getProductionOrder());
            
            WorkScheduleDTO created = workScheduleClient.createSchedule(scheduleDTO);
            log.info("Schedule created successfully with ID: {}", created.getId());
            
            String action = String.format("Създаден график - Производствена поръчка: %s, Смяна: %s, Дата: %s, Работен център: %s",
                    scheduleDTO.getProductionOrder(), scheduleDTO.getShift(), scheduleDTO.getDate(), scheduleDTO.getWorkCenter());
            logAction(userDetails, action);
            
            return new ModelAndView("redirect:/schedules?success=created");
        } catch (HttpClientErrorException e) {
            log.error("HTTP error creating schedule: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            String errorMsg = "Грешка при създаване на график: " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage());
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", false);
            modelAndView.addObject("error", errorMsg);
            return modelAndView;
        } catch (ResourceAccessException e) {
            log.error("Connection error creating schedule: {}", e.getMessage(), e);
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", false);
            modelAndView.addObject("error", "Грешка при свързване със сървиса за графици. Моля, проверете дали WorkScheduleService е стартиран.");
            return modelAndView;
        } catch (Exception e) {
            log.error("Error creating schedule: {}", e.getMessage(), e);
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", false);
            modelAndView.addObject("error", "Грешка: " + (e.getMessage() != null ? e.getMessage() : "Неизвестна грешка"));
            return modelAndView;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public String deleteSchedule(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting schedule {} via Feign Client", id);
        
        try {
            WorkScheduleDTO schedule = null;
            try {
                schedule = workScheduleClient.getScheduleById(id);
            } catch (Exception e) {
                log.warn("Could not fetch schedule info before deletion: {}", e.getMessage());
            }
            
            workScheduleClient.deleteSchedule(id);
            log.info("Schedule deleted successfully with ID: {}", id);
            
            String action = schedule != null 
                ? String.format("Изтрит график (ID: %s) - Производствена поръчка: %s, Смяна: %s, Дата: %s",
                    id, schedule.getProductionOrder(), schedule.getShift(), schedule.getDate())
                : String.format("Изтрит график (ID: %s)", id);
            logAction(userDetails, action);
            
            return "redirect:/schedules?success=deleted";
        } catch (Exception e) {
            log.error("Error deleting schedule: {}", e.getMessage(), e);
            return "redirect:/schedules?error=" + e.getMessage();
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public ModelAndView showEditScheduleForm(@PathVariable UUID id) {
        log.debug("Showing edit schedule form for ID: {}", id);
        
        try {
            WorkScheduleDTO schedule = workScheduleClient.getScheduleById(id);
            
            if (schedule.getWorkCenter() != null) {
                workCenterService.findAllActive().stream()
                        .filter(wc -> schedule.getWorkCenter().equals(wc.getNumber()))
                        .findFirst()
                        .ifPresent(wc -> schedule.setWorkCenterId(wc.getId()));
            }
            
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", schedule);
            modelAndView.addObject("isEdit", true);
            return modelAndView;
        } catch (Exception e) {
            log.error("Error loading schedule for edit: {}", e.getMessage(), e);
            return new ModelAndView("redirect:/schedules?error=notfound");
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public ModelAndView updateSchedule(@PathVariable UUID id, 
                                @ModelAttribute WorkScheduleDTO scheduleDTO,
                                @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating schedule {} via Feign Client", id);
        
        if (scheduleDTO.getWorkCenterId() == null) {
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", true);
            modelAndView.addObject("error", "Моля, изберете работен център");
            return modelAndView;
        }
        
        WorkCenter selectedWorkCenter = workCenterService.findById(scheduleDTO.getWorkCenterId())
                .orElseThrow(() -> new WorkCenterNotFoundException("Work center not found: " + scheduleDTO.getWorkCenterId()));
        
        scheduleDTO.setWorkCenter(selectedWorkCenter.getNumber());
        
        try {
            scheduleDTO.setId(id);
            WorkScheduleDTO updated = workScheduleClient.updateSchedule(id, scheduleDTO);
            log.info("Schedule updated successfully with ID: {}", updated.getId());
            
            String action = String.format("Променен график (ID: %s) - Производствена поръчка: %s, Смяна: %s, Дата: %s, Работен център: %s",
                    id, scheduleDTO.getProductionOrder(), scheduleDTO.getShift(), scheduleDTO.getDate(), scheduleDTO.getWorkCenter());
            logAction(userDetails, action);
            
            return new ModelAndView("redirect:/schedules?success=updated");
        } catch (Exception e) {
            log.error("Error updating schedule: {}", e.getMessage(), e);
            ModelAndView modelAndView = new ModelAndView("schedule-form");
            modelAndView.addObject("workCenters", workCenterService.findAllActive());
            modelAndView.addObject("schedule", scheduleDTO);
            modelAndView.addObject("isEdit", true);
            modelAndView.addObject("error", "Грешка: " + (e.getMessage() != null ? e.getMessage() : "Неизвестна грешка"));
            return modelAndView;
        }
    }
}

