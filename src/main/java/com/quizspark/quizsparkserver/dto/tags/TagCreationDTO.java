package com.quizspark.quizsparkserver.dto.tags;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagCreationDTO {
    @NotBlank(message = "Tag name is required")
    private String name;

    private String description;

    private String color;

    public Tag toTag(QuestionBank bank) {
        Tag tag = new Tag();
        tag.setBank(bank);
        tag.setName(name);
        if (description != null)
            tag.setDescription(description);
        if (color != null)
            tag.setColor(color);
        return tag;
    }
}
