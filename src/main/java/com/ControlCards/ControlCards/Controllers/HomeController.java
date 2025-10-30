package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.DTO.LoginDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginDTO", new LoginDTO());
        return modelAndView;
    }

    @GetMapping("/info")
    public String info() {
        return "info";
    }
}
