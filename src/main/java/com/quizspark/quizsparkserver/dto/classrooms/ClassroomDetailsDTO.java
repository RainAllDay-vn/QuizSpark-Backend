package com.quizspark.quizsparkserver.dto.classrooms;

import com.quizspark.quizsparkserver.models.Classroom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ClassroomDetailsDTO {
    private UUID id;
    private String name;
    private String description;
    private String joinCode;
    private Date createdAt;
    private List<ClassroomMemberDTO> members;

    public ClassroomDetailsDTO(Classroom classroom) {
        this.id = classroom.getId();
        this.name = classroom.getName();
        this.description = classroom.getDescription();
        this.joinCode = classroom.getJoinCode();
        this.createdAt = classroom.getCreatedAt();
        if (classroom.getMembers() != null) {
            this.members = classroom.getMembers().stream()
                    .map(ClassroomMemberDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
