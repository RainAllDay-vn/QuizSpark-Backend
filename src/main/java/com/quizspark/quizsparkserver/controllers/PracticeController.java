package com.quizspark.quizsparkserver.controllers;

import com.quizspark.quizsparkserver.models.Collection;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Controller
@RequestMapping
public class PracticeController {
    private final QuizService quizService;

    @Autowired
    public PracticeController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/practice")
    @PreAuthorize("hasRole('ADMIN')")
    public String practicePage(Model model, @RequestParam(required = false) String collectionId) {
        Collection collection = collectionId!=null
                ? quizService.getCollectionById(collectionId)
                : quizService.getPublicCollections().get(0);
        model.addAttribute("id", collection.getId());
        model.addAttribute("name", collection.getName());
        model.addAttribute("description", collection.getDescription());
        return "pages/practice";
    }
}
