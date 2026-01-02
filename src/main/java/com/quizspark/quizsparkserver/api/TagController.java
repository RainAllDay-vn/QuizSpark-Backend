package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.tags.TagCreationDTO;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import com.quizspark.quizsparkserver.dto.tags.TagUpdateDTO;
import com.quizspark.quizsparkserver.services.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/bank/{bankId}")
    public TagDTO addTag(@PathVariable String bankId, @Valid @RequestBody TagCreationDTO tagCreationDTO) {
        return tagService.addTag(bankId, tagCreationDTO);
    }

    @PutMapping("/single/{tagId}")
    public TagDTO updateTag(@PathVariable String tagId, @Valid @RequestBody TagUpdateDTO tagUpdateDTO) {
        return tagService.updateTag(tagId, tagUpdateDTO);
    }

    @DeleteMapping("/single/{tagId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTag(@PathVariable String tagId) {
        tagService.deleteTag(tagId);
    }
}
