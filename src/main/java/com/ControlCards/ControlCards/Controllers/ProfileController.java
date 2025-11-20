package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.DTO.ProfileUpdateDTO;
import com.ControlCards.ControlCards.Exception.UserNotFoundException;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LogEntryService logEntryService;

    @Autowired
    public ProfileController(UserService userService,
                             PasswordEncoder passwordEncoder,
                             LogEntryService logEntryService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.logEntryService = logEntryService;
    }

    @GetMapping
    public ModelAndView showProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Showing profile for user: {}", userDetails.getUsername());
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        ProfileUpdateDTO profileDTO = new ProfileUpdateDTO();
        profileDTO.setFirstName(currentUser.getFirstName());
        profileDTO.setLastName(currentUser.getLastName());
        
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("user", currentUser);
        modelAndView.addObject("profileDTO", profileDTO);
        return modelAndView;
    }

    @PostMapping
    public ModelAndView updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @ModelAttribute("profileDTO") ProfileUpdateDTO profileDTO,
                                     BindingResult bindingResult) {
        log.info("Updating profile for user: {}", userDetails.getUsername());
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        if (profileDTO.getPassword() != null && !profileDTO.getPassword().isEmpty()) {
            if (profileDTO.getPassword().length() < 6) {
                bindingResult.rejectValue("password", "error.password", "Паролата трябва да бъде поне 6 символа");
            }
        }
        
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in profile update for user: {}", userDetails.getUsername());
            ModelAndView modelAndView = new ModelAndView("profile");
            modelAndView.addObject("user", currentUser);
            modelAndView.addObject("profileDTO", profileDTO);
            return modelAndView;
        }
        
        String oldFirstName = currentUser.getFirstName();
        String oldLastName = currentUser.getLastName();
        
        currentUser.setFirstName(profileDTO.getFirstName().trim());
        currentUser.setLastName(profileDTO.getLastName().trim());
        
        if (profileDTO.getPassword() != null && !profileDTO.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
            log.info("Password updated for user: {}", userDetails.getUsername());
        }
        
        userService.save(currentUser);
        log.info("Profile updated for user: {} - {} {} -> {} {}", 
                userDetails.getUsername(), oldFirstName, oldLastName, 
                currentUser.getFirstName(), currentUser.getLastName());
        
        logEntryService.createLog(currentUser, "Обновен профил: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        
        ProfileUpdateDTO updatedDTO = new ProfileUpdateDTO();
        updatedDTO.setFirstName(currentUser.getFirstName());
        updatedDTO.setLastName(currentUser.getLastName());
        
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("user", currentUser);
        modelAndView.addObject("profileDTO", updatedDTO);
        modelAndView.addObject("success", "Профилът е обновен успешно!");
        return modelAndView;
    }
}

