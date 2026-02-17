package org.example.aitestgenerator.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service // This tells Spring to manage this class as a business logic component
public class TestGenerationService {

    private final ChatClient chatClient;

    public TestGenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // Core business logic: Talk to AI
    public String generateRawTest(String javaMethod) {
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

    // Core business logic: Clean, parse, and save
    public String generateAndSaveTestFile(String javaMethod) {
        String rawResponse = generateRawTest(javaMethod);
        String cleanedCode = cleanAiOutput(rawResponse);
        String className = extractClassName(cleanedCode);

        if (className == null) {
            return "Error: Could not determine class name from generated code.\n\n" + cleanedCode;
        }

        return saveFileToDisk(className, cleanedCode);
    }

    // --- Private Helper Methods (Keeps the main methods clean) ---

    private String cleanAiOutput(String rawCode) {
        return rawCode.replaceAll("```java\n?", "")
                .replaceAll("```\n?", "")
                .trim();
    }

    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public class ([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String saveFileToDisk(String className, String code) {
        try {
            Path directoryPath = Paths.get("src/test/java/org/example/aitestgenerator/generated");
            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(className + ".java");
            Files.writeString(filePath, code);

            return "Success! Test file generated and saved to: " + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "Failed to save file: " + e.getMessage();
        }
    }
}