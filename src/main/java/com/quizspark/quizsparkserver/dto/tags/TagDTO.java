package com.quizspark.quizsparkserver.dto.tags;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizspark.quizsparkserver.models.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO implements Comparable<TagDTO> {
    private UUID id;
    private String name;
    private String description;
    private String color;

    public TagDTO(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.description = tag.getDescription();
        this.color = tag.getColor();
    }

    @Override
    public int compareTo(TagDTO o) {
        return this.name.compareTo(o.getName());
    }
}
