package com.quizspark.quizsparkserver.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiResponseDTO {
    private String status;
    private String message;
    private String data;
    private Integer current;
    private Integer total;
}
