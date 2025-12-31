package com.quizspark.quizsparkserver.dto;

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
    private boolean isImage;
    private TextDensity textDensity;
}