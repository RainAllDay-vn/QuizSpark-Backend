package com.quizspark.quizsparkserver.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDTO {
    public enum ModelCharacteristic {
        VISION,
        REASONING
    }

    private String id;
    private String name;
    private String displayName;
    private List<ModelCharacteristic> characteristics;
}
