package com.quizspark.quizsparkserver.dto;

import com.quizspark.quizsparkserver.models.ClassroomMember;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ClassroomMemberDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private ClassroomMember.ClassroomRole role;
    private Date joinedAt;

    public ClassroomMemberDTO(ClassroomMember member) {
        this.id = member.getId();
        this.username = member.getUser().getUsername();
        this.firstName = member.getUser().getFirstName();
        this.lastName = member.getUser().getLastName();
        this.role = member.getRole();
        this.joinedAt = member.getJoinedAt();
    }
}
