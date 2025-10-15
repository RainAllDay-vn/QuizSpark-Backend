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
    private final QuizService quizService;

    @Autowired
    public HomeController(UserService userService, QuizService quizService) {
        this.userService = userService;
        this.quizService = quizService;
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

    @GetMapping("/practice")
    @PreAuthorize("hasRole('ADMIN')")
    public String practicePage(Model model, @RequestParam(required = false) String collectionId) {
        Collection collection;
        if (collectionId != null) {
            collection = quizService.getCollectionById(collectionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
        } else {
            collection = quizService.getPublicCollections().get(0);
        }
        model.addAttribute("id", collection.getId());
        model.addAttribute("name", collection.getName());
        model.addAttribute("description", collection.getDescription());
        return "pages/practice";
    }
}
