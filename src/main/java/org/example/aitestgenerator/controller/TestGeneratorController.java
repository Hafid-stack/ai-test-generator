package org.example.aitestgenerator.controller;

import org.example.aitestgenerator.service.TestGenerationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generate")
public class TestGeneratorController {

    private final TestGenerationService testGenerationService;

    // Inject the Service, not the ChatClient directly!
    public TestGeneratorController(TestGenerationService testGenerationService) {
        this.testGenerationService = testGenerationService;
    }

    @PostMapping("/test")
    public String generateTest(@RequestBody String javaMethod) {
        // Just delegate to the service
        return testGenerationService.generateRawTest(javaMethod);
    }

    @PostMapping("/test-file")
    public String generateAndSaveTest(@RequestBody String javaMethod) {
        // Just delegate to the service
        return testGenerationService.generateAndSaveTestFile(javaMethod);
    }
}