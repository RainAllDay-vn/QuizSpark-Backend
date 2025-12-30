package com.quizspark.quizsparkserver.api;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String MODEL_NAME = "deepseek-ai/DeepSeek-V3.2-TEE";
    private final ChatModel chatModel;

    public AiController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/generate")
    public Map<String, String> generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(message, OpenAiChatOptions.builder().model(MODEL_NAME).build());
        String response = chatModel.call(prompt).getResult().getOutput().getContent();
        return Map.of("generation", response);
    }
}
