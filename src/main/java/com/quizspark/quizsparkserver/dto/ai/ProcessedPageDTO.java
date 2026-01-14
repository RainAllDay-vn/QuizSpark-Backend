package com.quizspark.quizsparkserver.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProcessedPageDTO {
    public enum TextDensity {
        HIGH,
        MEDIUM,
        LOW
    }

    private String text;
    private boolean requireVision;
    private TextDensity textDensity;
    private boolean extracted;

    public ProcessedPageDTO(String text, boolean requiredVision) {
        this.text = text;
        this.requireVision = requiredVision;
        this.textDensity = null;
        this.extracted = false;
    }

    @Override
    public String toString() {
        return "ProcessedPageDTO{" +
                "text='" + text + '\'' +
                ", requireVision=" + requireVision +
                ", textDensity=" + textDensity +
                ", extracted=" + extracted +
                '}';
    }
}