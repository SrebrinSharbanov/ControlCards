package com.ControlCards.ControlCards.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserRequestController {

    @GetMapping("/user-request")
    public String userRequest() {
        return "user-request";
    }
}