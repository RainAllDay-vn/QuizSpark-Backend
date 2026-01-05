package com.quizspark.quizsparkserver.dto.classrooms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClassroomCreateDTO {
    private String name;
    private String description;
}
