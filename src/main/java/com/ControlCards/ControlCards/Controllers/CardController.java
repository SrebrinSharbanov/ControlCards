package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Exception.UserNotFoundException;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Service.CardService;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cards")
@Slf4j
public class CardController {

    private final CardService cardService;
    private final UserService userService;
    private final WorkCenterService workCenterService;

    @Autowired
    public CardController(CardService cardService, UserService userService, 
                         WorkCenterService workCenterService) {
        this.cardService = cardService;
        this.userService = userService;
        this.workCenterService = workCenterService;
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('WORKER')")
    public ModelAndView showCreateForm(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Worker {} accessing card creation form", userDetails.getUsername());
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));

        List<Workshop> workshops = currentUser.getWorkshops();
        List<WorkCenter> workCenters = new ArrayList<>();
        for (Workshop workshop : workshops) {
            workCenters.addAll(workCenterService.findByWorkshopId(workshop.getId()));
        }

        ModelAndView modelAndView = new ModelAndView("worker-cards-new");
        modelAndView.addObject("workshops", workshops);
        modelAndView.addObject("workCenters", workCenters);
        modelAndView.addObject("shifts", Shift.values());
        modelAndView.addObject("card", new CardCreateDTO());
        return modelAndView;
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('WORKER')")
    public String createCard(@Valid CardCreateDTO cardCreateDTO, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));

        cardService.createCard(cardCreateDTO, currentUser);
        log.info("Card created successfully by worker: {}", currentUser.getUsername());

        return "redirect:/dashboard";
    }

    @GetMapping("/created")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ModelAndView listCreatedCards() {
        log.debug("Listing created cards for technician");
        List<CardViewDTO> createdCards = cardService.getCreatedCards();
        ModelAndView modelAndView = new ModelAndView("technician-cards");
        modelAndView.addObject("cards", createdCards);
        return modelAndView;
    }

    @GetMapping("/extend/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ModelAndView showExtendForm(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!cardService.cardExists(id)) {
            return new ModelAndView("redirect:/cards/created");
        }
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        if (!cardService.canExtendCard(id, currentUser)) {
            return new ModelAndView("redirect:/cards/created");
        }
        
        ModelAndView modelAndView = new ModelAndView("technician-cards-extend");
        modelAndView.addObject("cardId", id);
        return modelAndView;
    }

    @PostMapping("/extend/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public String extendCard(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        cardService.extendCard(id, currentUser);
        log.info("Card {} extended by technician: {}", id, currentUser.getUsername());
        
        return "redirect:/cards/created";
    }

    @GetMapping("/extended")
    @PreAuthorize("hasRole('MANAGER')")
    public ModelAndView listExtendedCards() {
        log.debug("Listing extended cards for manager");
        List<CardViewDTO> extendedCards = cardService.getExtendedCards();
        ModelAndView modelAndView = new ModelAndView("manager-cards");
        modelAndView.addObject("cards", extendedCards);
        return modelAndView;
    }

    @PostMapping("/close/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String closeCard(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        cardService.closeCard(id, currentUser);
        log.info("Card {} closed by manager: {}", id, currentUser.getUsername());
        
        return "redirect:/cards/extended";
    }
}

