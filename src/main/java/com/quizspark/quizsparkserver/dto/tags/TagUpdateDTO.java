package com.quizspark.quizsparkserver.dto.tags;

import com.quizspark.quizsparkserver.models.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagUpdateDTO {
    private String name;
    private String description;
    private String color;

    public void updateTag(Tag tag) {
        if (name != null && !name.isBlank()) {
            tag.setName(name);
        }
        if (description != null) {
            tag.setDescription(description);
        }
        if (color != null && !color.isBlank()) {
            tag.setColor(color);
        }
    }
}
