package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Exception.UserNotFoundException;
import com.ControlCards.ControlCards.Exception.WorkCenterNotFoundException;
import com.ControlCards.ControlCards.Exception.WorkshopNotFoundException;
import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        modelAndView.addObject("allWorkshops", workshopService.findAll());
        return modelAndView;
    }

    @PostMapping("/users/new")
    public String createUser(@ModelAttribute User user, 
                             @RequestParam(required = false) List<UUID> workshopIds,
                             @RequestParam(required = false) Boolean selectAll,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        if (Boolean.TRUE.equals(selectAll)) {
            user.setWorkshops(workshopService.findAll());
        } else if (workshopIds != null && !workshopIds.isEmpty()) {
            List<Workshop> workshops = workshopIds.stream()
                    .map(workshopId -> workshopService.findById(workshopId))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            user.setWorkshops(workshops);
        } else {
            user.setWorkshops(new ArrayList<>());
        }
        
        userService.save(user);
        log.info("User created: {}", user.getUsername());
        logAction(userDetails, "Създаден нов потребител: " + user.getUsername());
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView showEditUserForm(@PathVariable UUID id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            log.warn("Attempt to edit non-existent user with ID: {}", id);
            return new ModelAndView("redirect:/admin/users");
        }
        
        Optional<User> userWithWorkshops = userService.findByUsernameWithWorkshops(userOpt.get().getUsername());
        if (userWithWorkshops.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("admin-users-form");
            modelAndView.addObject("user", userWithWorkshops.get());
            modelAndView.addObject("roles", Role.values());
            modelAndView.addObject("allWorkshops", workshopService.findAll());
            return modelAndView;
        }
        log.warn("User with ID {} found but could not load with workshops", id);
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable UUID id, @ModelAttribute User user,
                             @RequestParam(required = false) List<UUID> workshopIds,
                             @RequestParam(required = false) Boolean selectAll,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        Optional<User> existingUserWithWorkshops = userService.findByUsernameWithWorkshops(existingUser.getUsername());
        if (existingUserWithWorkshops.isEmpty()) {
            throw new UserNotFoundException("User not found with username: " + existingUser.getUsername());
        }
        
        existingUser = existingUserWithWorkshops.get();
        String oldUsername = existingUser.getUsername();
        existingUser.setUsername(user.getUsername());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setRole(user.getRole());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        if (Boolean.TRUE.equals(selectAll)) {
            existingUser.setWorkshops(workshopService.findAll());
        } else if (workshopIds != null && !workshopIds.isEmpty()) {
            List<Workshop> workshops = workshopIds.stream()
                    .map(workshopId -> workshopService.findById(workshopId))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            existingUser.setWorkshops(workshops);
        } else {
            existingUser.setWorkshops(new ArrayList<>());
        }
        
        userService.save(existingUser);
        log.info("User updated: {} -> {}", oldUsername, user.getUsername());
        logAction(userDetails, "Обновен потребител: " + oldUsername + " -> " + user.getUsername());
        return "redirect:/admin/users";
    }

    @PostMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userService.existsById(id)) {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getRole() == Role.ADMIN) {
                    log.warn("Attempt to deactivate ADMIN user blocked: {}", user.getUsername());
                    return "redirect:/admin/users";
                }
                String username = user.getUsername();
                try {
                    userService.deactivate(id);
                    log.info("User deactivated: {}", username);
                    logAction(userDetails, "Деактивиран потребител: " + username);
                } catch (RuntimeException e) {
                    log.error("Failed to deactivate user {}: {}", username, e.getMessage());
                }
            }
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/activate/{id}")
    public String activateUser(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userService.existsById(id)) {
            Optional<User> userOpt = userService.findById(id);
            String username = userOpt.map(User::getUsername).orElse("Unknown");
            userService.activate(id);
            log.info("User activated: {}", username);
            logAction(userDetails, "Активиран потребител: " + username);
        }
        return "redirect:/admin/users";
    }

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
        List<Workshop> workshops = workshopService.findAllActive();
        modelAndView.addObject("workshops", workshops);
        return modelAndView;
    }

    @PostMapping("/workcenters/new")
    public String createWorkCenter(@ModelAttribute WorkCenter workCenter, @RequestParam UUID workshopId, 
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Workshop workshop = workshopService.findById(workshopId)
                .orElseThrow(() -> new WorkshopNotFoundException("Workshop not found: " + workshopId));
        workCenter.setWorkshop(workshop);
        workCenterService.save(workCenter);
        log.info("Work center created: {}", workCenter.getNumber());
        logAction(userDetails, "Създаден нов работен център: " + workCenter.getNumber());
        return "redirect:/admin/workcenters";
    }

    @GetMapping("/workcenters/edit/{id}")
    public ModelAndView showEditWorkCenterForm(@PathVariable UUID id) {
        Optional<WorkCenter> workCenterOpt = workCenterService.findById(id);
        if (workCenterOpt.isEmpty()) {
            log.warn("Attempt to edit non-existent work center with ID: {}", id);
            return new ModelAndView("redirect:/admin/workcenters");
        }
        
        ModelAndView modelAndView = new ModelAndView("admin-workcenters-form");
        modelAndView.addObject("workCenter", workCenterOpt.get());
        List<Workshop> workshops = workshopService.findAllActive();
        modelAndView.addObject("workshops", workshops);
        return modelAndView;
    }

    @PostMapping("/workcenters/edit/{id}")
    public String updateWorkCenter(@PathVariable UUID id, @ModelAttribute WorkCenter workCenter, 
                                   @RequestParam UUID workshopId, @AuthenticationPrincipal UserDetails userDetails) {
        WorkCenter existingWorkCenter = workCenterService.findById(id)
                .orElseThrow(() -> new WorkCenterNotFoundException("Work center not found with ID: " + id));
        
        String oldNumber = existingWorkCenter.getNumber();
        existingWorkCenter.setNumber(workCenter.getNumber());
        existingWorkCenter.setDescription(workCenter.getDescription());
        existingWorkCenter.setMachineType(workCenter.getMachineType());
        
        Workshop workshop = workshopService.findById(workshopId)
                .orElseThrow(() -> new WorkshopNotFoundException("Workshop not found: " + workshopId));
        existingWorkCenter.setWorkshop(workshop);
        
        workCenterService.save(existingWorkCenter);
        log.info("Work center updated: {} -> {}", oldNumber, workCenter.getNumber());
        logAction(userDetails, "Обновен работен център: " + oldNumber + " -> " + workCenter.getNumber());
        return "redirect:/admin/workcenters";
    }

    @PostMapping("/workcenters/deactivate/{id}")
    public String deactivateWorkCenter(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workCenterService.existsById(id)) {
            Optional<WorkCenter> workCenterOpt = workCenterService.findById(id);
            String workCenterNumber = workCenterOpt.map(WorkCenter::getNumber).orElse("Unknown");
            workCenterService.deactivate(id);
            log.info("Work center deactivated: {}", workCenterNumber);
            logAction(userDetails, "Деактивиран работен център: " + workCenterNumber);
        }
        return "redirect:/admin/workcenters";
    }

    @PostMapping("/workcenters/activate/{id}")
    public String activateWorkCenter(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workCenterService.existsById(id)) {
            Optional<WorkCenter> workCenterOpt = workCenterService.findById(id);
            String workCenterNumber = workCenterOpt.map(WorkCenter::getNumber).orElse("Unknown");
            workCenterService.activate(id);
            log.info("Work center activated: {}", workCenterNumber);
            logAction(userDetails, "Активиран работен център: " + workCenterNumber);
        }
        return "redirect:/admin/workcenters";
    }

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
        logAction(userDetails, "Създаден нов цех: " + workshop.getName());
        return "redirect:/admin/workshops";
    }

    @GetMapping("/workshops/edit/{id}")
    public ModelAndView showEditWorkshopForm(@PathVariable UUID id) {
        Optional<Workshop> workshopOpt = workshopService.findById(id);
        if (workshopOpt.isEmpty()) {
            log.warn("Attempt to edit non-existent workshop with ID: {}", id);
            return new ModelAndView("redirect:/admin/workshops");
        }
        
        ModelAndView modelAndView = new ModelAndView("admin-workshops-form");
        modelAndView.addObject("workshop", workshopOpt.get());
        return modelAndView;
    }

    @PostMapping("/workshops/edit/{id}")
    public String updateWorkshop(@PathVariable UUID id, @ModelAttribute Workshop workshop, @AuthenticationPrincipal UserDetails userDetails) {
        Workshop existingWorkshop = workshopService.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException("Workshop not found with ID: " + id));
        
        String oldName = existingWorkshop.getName();
        existingWorkshop.setName(workshop.getName());
        existingWorkshop.setDescription(workshop.getDescription());
        workshopService.save(existingWorkshop);
        log.info("Workshop updated: {} -> {}", oldName, workshop.getName());
        logAction(userDetails, "Обновен цех: " + oldName + " -> " + workshop.getName());
        return "redirect:/admin/workshops";
    }

    @PostMapping("/workshops/deactivate/{id}")
    public String deactivateWorkshop(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workshopService.existsById(id)) {
            Optional<Workshop> workshopOpt = workshopService.findById(id);
            String workshopName = workshopOpt.map(Workshop::getName).orElse("Unknown");
            workshopService.deactivate(id);
            log.info("Workshop deactivated: {}", workshopName);
            logAction(userDetails, "Деактивиран цех: " + workshopName);
        }
        return "redirect:/admin/workshops";
    }

    @PostMapping("/workshops/activate/{id}")
    public String activateWorkshop(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (workshopService.existsById(id)) {
            Optional<Workshop> workshopOpt = workshopService.findById(id);
            String workshopName = workshopOpt.map(Workshop::getName).orElse("Unknown");
            workshopService.activate(id);
            log.info("Workshop activated: {}", workshopName);
            logAction(userDetails, "Активиран цех: " + workshopName);
        }
        return "redirect:/admin/workshops";
    }

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

