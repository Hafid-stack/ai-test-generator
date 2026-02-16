package org.example.aitestgenerator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generate")
public class TestGeneratorController {

    private final ChatClient chatClient;

    public TestGeneratorController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/test")
    public String generateTest(@RequestBody String javaMethod) {
        String systemPrompt = "You are an expert Java developer and QA engineer. " +
                "Write a JUnit 5 test for the following Java method. " +
                "Return ONLY the raw Java code. " +
                "Do not include markdown formatting, explanations, or backticks.";

        return chatClient.prompt()
                .system(systemPrompt)
                .user(javaMethod)
                .call()
                .content();
    }
}