package com.quizspark.quizsparkserver.controllers;

import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class HomeController {
    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String landingPage(Model model) {
        model.addAttribute("status", userService.getUserDatabaseStatus());
        return "landing";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}
