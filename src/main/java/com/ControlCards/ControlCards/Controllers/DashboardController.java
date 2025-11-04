package com.ControlCards.ControlCards.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        if (authentication != null && authentication.isAuthenticated()) {
            modelAndView.addObject("username", authentication.getName());
            modelAndView.addObject("authorities", authentication.getAuthorities());
        }
        return modelAndView;
    }
}
