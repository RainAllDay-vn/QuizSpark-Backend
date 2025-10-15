package com.quizspark.quizsparkserver.controllers;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.services.QuizService;
import com.quizspark.quizsparkserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
        return "pages/landing";
    }

    @GetMapping("/home")
    public String homePage() {
        return "pages/home";
    }
}
