package com.quizspark.quizsparkserver.dto;

import com.quizspark.quizsparkserver.models.QuestionBank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class QuestionBankUpdateDTO {
    @Pattern(regexp = "PRIVATE|PUBLIC", message = "Question bank must either be PRIVATE or PUBLIC")
    private String access;

    @Pattern(regexp = "DRAFT|PUBLISHED", message = "Question bank status must either be DRAFT or PUBLISHED")
    private String status;

    @Size(max = 200, message = "Question bank name must be less than 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    public void updateQuestionBank(QuestionBank bank) {
        if (access != null) {
            bank.setAccess(QuestionBank.Access.valueOf(access));
        }
        if (status != null) {
            bank.setStatus(QuestionBank.Status.valueOf(status));
        }
        if (name != null && !name.isBlank()) {
            bank.setName(name);
        }
        if (description != null) {
            if (description.isBlank()) {
                bank.setDescription(null);
            } else {
                bank.setDescription(description);
            }
        }
    }
}
