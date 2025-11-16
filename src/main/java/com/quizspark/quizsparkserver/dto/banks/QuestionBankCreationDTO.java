package com.quizspark.quizsparkserver.dto.banks;

import com.quizspark.quizsparkserver.models.QuestionBank;
import com.quizspark.quizsparkserver.models.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class QuestionBankCreationDTO {
    @Pattern(regexp = "PRIVATE|PUBLIC", message = "Question bank must either be PRIVATE or PUBLIC")
    private String access;

    @NotBlank(message = "Question bank name is required")
    @Size(max = 200, message = "Question bank name must be less than 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    public QuestionBank toQuestionBank(User user) {
        QuestionBank bank = new QuestionBank();
        bank.setUser(user);
        bank.setStatus(QuestionBank.Status.DRAFT);
        bank.setAccess(QuestionBank.Access.valueOf(access));
        bank.setName(name);
        if (description!=null && !description.isBlank()) bank.setDescription(description);
        return bank;
    }
}
