package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Exception.UserNotFoundException;
import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import com.ControlCards.ControlCards.Util.Enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final WorkCenterService workCenterService;
    private final WorkshopService workshopService;
    private final LogEntryService logEntryService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserService userService, WorkCenterService workCenterService, 
                          WorkshopService workshopService, LogEntryService logEntryService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.workCenterService = workCenterService;
        this.workshopService = workshopService;
        this.logEntryService = logEntryService;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== HELPER METHODS ==========
    
    private void logAction(UserDetails userDetails, String action) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
            logEntryService.createLog(currentUser, action);
            log.info("Admin action logged: {} by user: {}", action, currentUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to log action: {}", e.getMessage());
        }
    }

    // ========== USER MANAGEMENT ==========
    
    @GetMapping("/users")
    public ModelAndView listUsers() {
        log.debug("Listing all users");
        List<User> users = userService.findAll();
        ModelAndView modelAndView = new ModelAndView("admin-users");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @GetMapping("/users/new")
    public ModelAndView showCreateUserForm() {
        log.debug("Showing create user form");
        ModelAndView modelAndView = new ModelAndView("admin-users-form");
        modelAndView.addObject("user", new User());
        modelAndView.addObject("roles", Role.values());
        return modelAndView;
    }

    @PostMapping("/users/new")
    public String createUser(@ModelAttribute User user, @AuthenticationPrincipal UserDetails userDetails) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userService.save(user);
        log.info("User created: {}", user.getUsername());
        logAction(userDetails, "Created new user: " + user.getUsername());
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView showEditUserForm(@PathVariable UUID id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("admin-users-form");
            modelAndView.addObject("user", userOpt.get());
            modelAndView.addObject("roles", Role.values());
            return modelAndView;
        }
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable UUID id, @ModelAttribute User user, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> existingUserOpt = userService.findById(id);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            String oldUsername = existingUser.getUsername();
            existingUser.setUsername(user.getUsername());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setRole(user.getRole());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userService.save(existingUser);
            log.info("User updated: {} -> {}", oldUsername, user.getUsername());
            logAction(userDetails, "Updated user: " + oldUsername + " -> " + user.getUsername());
        }
        return "redirect:/admin/users";
    }

    @DeleteMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userService.existsById(id)) {
            Optional<User> userOpt = userService.findById(id);
            String username = userOpt.map(User::getUsername).orElse("Unknown");
            userService.deleteById(id);
            log.info("User deleted: {}", username);
            logAction(userDetails, "Deleted user: " + username);
        }
        return "redirect:/admin/users";
    }

    // ========== WORK CENTER MANAGEMENT ==========
    
    @GetMapping("/workcenters")
    public ModelAndView listWorkCenters() {
        log.debug("Listing all work centers");
        List<WorkCenter> workCenters = workCenterService.findAll();
        ModelAndView modelAndView = new ModelAndView("admin-workcenters");
        modelAndView.addObject("workCenters", workCenters);
        return modelAndView;
    }

    @GetMapping("/workcenters/new")
    public ModelAndView showCreateWorkCenterForm() {
        log.debug("Showing create work center form");
        ModelAndView modelAndView = new ModelAndView("admin-workcenters-form");
        modelAndView.addObject("workCenter", new WorkCenter());
        List<Workshop> workshops = workshopService.findAll();
        modelAndView.addObject("workshops", workshops);
        return modelAndView;
    }

    @PostMapping("/workcenters/new")
    public String createWorkCenter(@ModelAttribute WorkCenter workCenter, @RequestParam UUID workshopId, 
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Workshop workshop = workshopService.findById(workshopId)
                .orElseThrow(() -> new RuntimeException("Workshop not found"));
        workCenter.setWorkshop(workshop);
        workCenterService.save(workCenter);
        log.info("Work center created: {}", workCenter.getNumber());
        logAction(userDetails, "Created new work center: " + workCenter.getNumber());
        return "redirect:/admin/workcenters";
    }

    @GetMapping("/workcenters/edit/{id}")
    public ModelAndView showEditWorkCenterForm(@PathVariable UUID id) {
        Optional<WorkCenter> workCenterOpt = workCenterService.findById(id);
        if (workCenterOpt.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("admin-workcenters-form");
            modelAndView.addObject("workCenter", workCenterOpt.get());
            List<Workshop> workshops = workshopService.findAll();
            modelAndView.addObject("workshops", workshops);
            return modelAndView;
        }
        return new ModelAndView("redirect:/admin/workcenters");
    }

    @PostMapping("/workcenters/edit/{id}")
    public String updateWorkCenter(@PathVariable UUID id, @ModelAttribute WorkCenter workCenter, 
                                   @RequestParam UUID workshopId, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<WorkCenter> existingWorkCenterOpt = workCenterService.findById(id);
        if (existingWorkCenterOpt.isPresent()) {
            WorkCenter existingWorkCenter = existingWorkCenterOpt.get();
            String oldNumber = existingWorkCenter.getNumber();
            existingWorkCenter.setNumber(workCenter.getNumber());
            existingWorkCenter.setDescription(workCenter.getDescription());
            existingWorkCenter.setMachineType(workCenter.getMachineType());
            
            Workshop workshop = workshopService.findById(workshopId)
                    .orElseThrow(() -> new RuntimeException("Workshop not found"));
            existingWorkCenter.setWorkshop(workshop);
            
            workCenterService.save(existingWorkCenter);
            log.info("Work center updated: {} -> {}", oldNumber, workCenter.getNumber());
            logAction(userDetails, "Updated work center: " + oldNumber + " -> " + workCenter.getNumber());
        }
        return "redirect:/admin/workcenters";
    }

    @DeleteMapping("/workcenters/delete/{id}")
    public String deleteWorkCenter(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workCenterService.existsById(id)) {
            Optional<WorkCenter> workCenterOpt = workCenterService.findById(id);
            String workCenterNumber = workCenterOpt.map(WorkCenter::getNumber).orElse("Unknown");
            workCenterService.deleteById(id);
            log.info("Work center deleted: {}", workCenterNumber);
            logAction(userDetails, "Deleted work center: " + workCenterNumber);
        }
        return "redirect:/admin/workcenters";
    }

    // ========== WORKSHOP MANAGEMENT ==========
    
    @GetMapping("/workshops")
    public ModelAndView listWorkshops() {
        log.debug("Listing all workshops");
        List<Workshop> workshops = workshopService.findAll();
        ModelAndView modelAndView = new ModelAndView("admin-workshops");
        modelAndView.addObject("workshops", workshops);
        return modelAndView;
    }

    @GetMapping("/workshops/new")
    public ModelAndView showCreateWorkshopForm() {
        log.debug("Showing create workshop form");
        ModelAndView modelAndView = new ModelAndView("admin-workshops-form");
        modelAndView.addObject("workshop", new Workshop());
        return modelAndView;
    }

    @PostMapping("/workshops/new")
    public String createWorkshop(@ModelAttribute Workshop workshop, @AuthenticationPrincipal UserDetails userDetails) {
        workshopService.save(workshop);
        log.info("Workshop created: {}", workshop.getName());
        logAction(userDetails, "Created new workshop: " + workshop.getName());
        return "redirect:/admin/workshops";
    }

    @GetMapping("/workshops/edit/{id}")
    public ModelAndView showEditWorkshopForm(@PathVariable UUID id) {
        Optional<Workshop> workshopOpt = workshopService.findById(id);
        if (workshopOpt.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("admin-workshops-form");
            modelAndView.addObject("workshop", workshopOpt.get());
            return modelAndView;
        }
        return new ModelAndView("redirect:/admin/workshops");
    }

    @PostMapping("/workshops/edit/{id}")
    public String updateWorkshop(@PathVariable UUID id, @ModelAttribute Workshop workshop, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Workshop> existingWorkshopOpt = workshopService.findById(id);
        if (existingWorkshopOpt.isPresent()) {
            Workshop existingWorkshop = existingWorkshopOpt.get();
            String oldName = existingWorkshop.getName();
            existingWorkshop.setName(workshop.getName());
            existingWorkshop.setDescription(workshop.getDescription());
            workshopService.save(existingWorkshop);
            log.info("Workshop updated: {} -> {}", oldName, workshop.getName());
            logAction(userDetails, "Updated workshop: " + oldName + " -> " + workshop.getName());
        }
        return "redirect:/admin/workshops";
    }

    @DeleteMapping("/workshops/delete/{id}")
    public String deleteWorkshop(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workshopService.existsById(id)) {
            Optional<Workshop> workshopOpt = workshopService.findById(id);
            String workshopName = workshopOpt.map(Workshop::getName).orElse("Unknown");
            workshopService.deleteById(id);
            log.info("Workshop deleted: {}", workshopName);
            logAction(userDetails, "Deleted workshop: " + workshopName);
        }
        return "redirect:/admin/workshops";
    }

    // ========== LOGS MANAGEMENT ==========
    
    @GetMapping("/logs")
    public ModelAndView listLogs() {
        log.debug("Listing all logs");
        List<LogEntry> logs = logEntryService.findAll();
        List<User> users = userService.findAll();
        ModelAndView modelAndView = new ModelAndView("admin-logs");
        modelAndView.addObject("logs", logs);
        modelAndView.addObject("users", users);
        return modelAndView;
    }
}

