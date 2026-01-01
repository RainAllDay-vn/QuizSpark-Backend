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
    private byte[] image;
    private TextDensity textDensity;

    @Override
    public String toString() {
        return "ProcessedPageDTO{" +
                "text=" + (text == null ? "null" : text.length()) +
                ", image=" + (image == null ? "null" : image.length) +
                ", textDensity=" + textDensity +
                '}';
    }
}