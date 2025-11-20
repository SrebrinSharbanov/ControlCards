package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardExtendDTO;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));

        List<Workshop> allWorkshops = currentUser.getWorkshops();
        List<Workshop> workshops = allWorkshops != null ? allWorkshops.stream()
                .filter(w -> w.getActive() != null && w.getActive())
                .collect(Collectors.toList()) : new ArrayList<>();
        log.debug("User {} has {} active workshops (out of {} total)", 
                 currentUser.getUsername(), workshops.size(), allWorkshops != null ? allWorkshops.size() : 0);
        
        List<WorkCenter> workCenters = getWorkCentersForWorkshops(workshops);

        ModelAndView modelAndView = new ModelAndView("worker-cards-new");
        modelAndView.addObject("workshops", workshops);
        modelAndView.addObject("workCenters", workCenters);
        modelAndView.addObject("shifts", Shift.values());
        modelAndView.addObject("card", new CardCreateDTO());
        return modelAndView;
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('WORKER')")
    public ModelAndView createCard(@Valid @ModelAttribute("card") CardCreateDTO cardCreateDTO, 
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Creating card with DTO: workshopId={}, workCenterId={}, shift={}, shortDescription={}", 
                 cardCreateDTO != null ? cardCreateDTO.getWorkshopId() : null, 
                 cardCreateDTO != null ? cardCreateDTO.getWorkCenterId() : null, 
                 cardCreateDTO != null ? cardCreateDTO.getShift() : null, 
                 cardCreateDTO != null ? cardCreateDTO.getShortDescription() : null);
        log.debug("BindingResult has errors: {}, error count: {}", 
                 bindingResult.hasErrors(), bindingResult.getErrorCount());
        
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));

        List<Workshop> allWorkshops = currentUser.getWorkshops();
        List<Workshop> workshops = allWorkshops != null ? allWorkshops.stream()
                .filter(w -> w.getActive() != null && w.getActive())
                .collect(Collectors.toList()) : new ArrayList<>();
        List<WorkCenter> workCenters = getWorkCentersForWorkshops(workshops);
        
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred during card creation. Error count: {}", bindingResult.getErrorCount());
            logValidationErrors(bindingResult);

            ModelAndView modelAndView = new ModelAndView("worker-cards-new");
            modelAndView.addObject("workshops", workshops);
            modelAndView.addObject("workCenters", workCenters);
            modelAndView.addObject("shifts", Shift.values());
            modelAndView.addObject("card", cardCreateDTO);
            return modelAndView;
        }

        cardService.createCard(cardCreateDTO, currentUser);
        log.info("Card created successfully by worker: {}", currentUser.getUsername());

        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/created")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ModelAndView listCreatedCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing created cards for technician");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> createdCards = cardService.getCreatedCards(currentUser);
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
        modelAndView.addObject("cardExtendDTO", new CardExtendDTO());
        return modelAndView;
    }

    @PostMapping("/extend/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ModelAndView extendCard(@PathVariable UUID id, 
                                   @Valid @ModelAttribute("cardExtendDTO") CardExtendDTO cardExtendDTO,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Extending card {} with DTO: detailedDescription={}, resolutionDurationMinutes={}", 
                 id, 
                 cardExtendDTO != null ? (cardExtendDTO.getDetailedDescription() != null ? cardExtendDTO.getDetailedDescription().length() + " chars" : "null") : null,
                 cardExtendDTO != null ? cardExtendDTO.getResolutionDurationMinutes() : null);
        log.debug("BindingResult has errors: {}, error count: {}", 
                 bindingResult.hasErrors(), bindingResult.getErrorCount());
        
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred during card extension for card: {}. Error count: {}", id, bindingResult.getErrorCount());
            logValidationErrors(bindingResult);
            
            ModelAndView modelAndView = new ModelAndView("technician-cards-extend");
            modelAndView.addObject("cardId", id);
            modelAndView.addObject("cardExtendDTO", cardExtendDTO);
            return modelAndView;
        }

        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        cardService.extendCard(id, cardExtendDTO, currentUser);
        log.info("Card {} extended by technician: {}", id, currentUser.getUsername());
        
        return new ModelAndView("redirect:/cards/created");
    }

    @GetMapping("/extended")
    @PreAuthorize("hasAnyRole('MANAGER', 'PRODUCTION_MANAGER')")
    public ModelAndView listExtendedCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing extended cards for manager/production manager");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> extendedCards = cardService.getExtendedCards(currentUser);
        ModelAndView modelAndView = new ModelAndView("manager-cards");
        modelAndView.addObject("cards", extendedCards);
        modelAndView.addObject("pageTitle", "Разширени карти");
        return modelAndView;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'MANAGER')")
    public ModelAndView listAllCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing all cards for admin/manager/production manager");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> allCards = cardService.getAllCards(currentUser);
        ModelAndView modelAndView = new ModelAndView("manager-cards");
        modelAndView.addObject("cards", allCards);
        modelAndView.addObject("pageTitle", "Всички карти");
        return modelAndView;
    }

    @GetMapping("/closed")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'MANAGER')")
    public ModelAndView listClosedCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing closed cards for admin/manager/production manager");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> closedCards = cardService.getClosedCards(currentUser);
        ModelAndView modelAndView = new ModelAndView("closed-cards");
        modelAndView.addObject("cards", closedCards);
        modelAndView.addObject("pageTitle", "Затворени карти");
        return modelAndView;
    }

    @GetMapping("/archived")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'MANAGER')")
    public ModelAndView listArchivedCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing archived cards for admin/manager/production manager");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> archivedCards = cardService.getArchivedCards(currentUser);
        ModelAndView modelAndView = new ModelAndView("archived-cards");
        modelAndView.addObject("cards", archivedCards);
        modelAndView.addObject("pageTitle", "Архивирани карти");
        return modelAndView;
    }

    @PostMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    public String closeCard(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        cardService.closeCard(id, currentUser);
        log.info("Card {} closed by user: {}", id, currentUser.getUsername());
        
        return "redirect:/cards/extended";
    }

    @PostMapping("/archive/{id}")
    @PreAuthorize("hasRole('PRODUCTION_MANAGER')")
    public String archiveCard(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        
        cardService.archiveCard(id, currentUser);
        log.info("Card {} archived by user: {}", id, currentUser.getUsername());
        
        return "redirect:/cards/closed";
    }

    @GetMapping("/view")
    @PreAuthorize("hasRole('WORKER')")
    public ModelAndView listWorkerCards(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Listing cards for worker");
        User currentUser = userService.findByUsernameWithWorkshops(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userDetails.getUsername()));
        List<CardViewDTO> workerCards = cardService.getAllCards(currentUser);
        ModelAndView modelAndView = new ModelAndView("manager-cards");
        modelAndView.addObject("cards", workerCards);
        modelAndView.addObject("pageTitle", "Преглед на карти");
        return modelAndView;
    }

    private List<WorkCenter> getWorkCentersForWorkshops(List<Workshop> workshops) {
        List<WorkCenter> workCenters = new ArrayList<>();
        if (workshops != null && !workshops.isEmpty()) {
            for (Workshop workshop : workshops) {
                List<WorkCenter> centers = workCenterService.findByWorkshopIdWithWorkshop(workshop.getId());
                workCenters.addAll(centers);
                log.debug("Workshop {} has {} active work centers", workshop.getName(), centers.size());
            }
        }
        return workCenters;
    }

    private void logValidationErrors(BindingResult bindingResult) {
        bindingResult.getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                log.debug("Validation error for field '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                log.debug("Validation error: {}", error);
            }
        });
    }
}

