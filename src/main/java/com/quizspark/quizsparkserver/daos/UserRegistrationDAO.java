package com.quizspark.quizsparkserver.daos;

import com.quizspark.quizsparkserver.models.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Setter
@NoArgsConstructor
public class UserRegistrationDAO {
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "STUDENT|TEACHER", message = "Account type must be either STUDENT or TEACHER")
    private String accountType; // "STUDENT" or "TEACHER"

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'-]+$", message = "First name can only contain letters, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ'-]+$", message = "Last name can only contain letters, hyphens, and apostrophes")
    private String lastName;

    private String dob; // Date of birth

    private String educationLevel;

    private String error;

    public User toUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole("ROLE_"+accountType);
        if (dob != null) user.setDob(Date.valueOf(dob));
        if (educationLevel != null) user.setEducationLevel(educationLevel);
        return user;
    }
}
