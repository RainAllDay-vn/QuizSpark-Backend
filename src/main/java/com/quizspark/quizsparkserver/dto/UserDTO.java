package com.quizspark.quizsparkserver.dto;

import com.quizspark.quizsparkserver.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String role;
    private String firstName;
    private String lastName;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.role = user.getRole();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
}
