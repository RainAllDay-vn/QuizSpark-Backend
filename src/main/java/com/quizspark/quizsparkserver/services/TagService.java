package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.tags.TagCreationDTO;
import com.quizspark.quizsparkserver.dto.tags.TagDTO;
import com.quizspark.quizsparkserver.dto.tags.TagUpdateDTO;
import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Tag;
import com.quizspark.quizsparkserver.repositories.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TagService {
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final TagRepository tagRepository;
    private final QuestionService questionService;

    public TagService(TagRepository tagRepository, QuestionService questionService) {
        this.tagRepository = tagRepository;
        this.questionService = questionService;
    }

    @Transactional
    public TagDTO addTag(String bankId, TagCreationDTO tagCreationDTO) {
        QuestionBank bank = questionService.getQuestionBankEntityById(bankId);
        if (!questionService.canBeEdited(bank)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to add tags to this bank");
        }
        Tag tag = tagCreationDTO.toTag(bank);
        tag = tagRepository.save(tag);
        return new TagDTO(tag);
    }

    @Transactional
    public TagDTO updateTag(String tagId, TagUpdateDTO tagUpdateDTO) {
        Tag tag = getTagEntityById(tagId);
        if (!questionService.canBeEdited(tag.getBank())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this tag");
        }
        tagUpdateDTO.updateTag(tag);
        tag = tagRepository.save(tag);
        return new TagDTO(tag);
    }

    @Transactional
    public void deleteTag(String tagId) {
        Tag tag = getTagEntityById(tagId);
        if (!questionService.canBeEdited(tag.getBank())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this tag");
        }
        
        // Use programmatic cleanup of the Many-to-Many association for ALL questions.
        // Similar to QuestionBank deletion, we must ensure associations from soft-deleted
        // questions are also removed to satisfy referential integrity in the join table.
        for (com.quizspark.quizsparkserver.models.Question question : tag.getQuestions()) {
            question.getTags().remove(tag);
        }
        
        // Flush join table updates before deleting the Tag entity
        entityManager.flush();

        tagRepository.delete(tag);
    }

    public Tag getTagEntityById(String tagId) {
        return tagRepository.findById(UUID.fromString(tagId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
    }
}
