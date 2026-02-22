package org.example.aitestgenerator.controller;

import org.example.aitestgenerator.compiler.InMemoryCompilerSandbox;
import org.example.aitestgenerator.dto.TestRequest;
import org.example.aitestgenerator.service.TestGenerationService;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/api/generate")
public class TestGeneratorController {

    private final TestGenerationService testGenerationService;


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
    @PostMapping("/test-with-context")
    public String generateAndSaveTestWithContext(@RequestBody TestRequest testRequest) {
        return testGenerationService.generateTestWithContext(testRequest);

    }
    @GetMapping("/sandbox")
    public String testInMemoryCompiler() {
        try {
            // A raw string of Java code that doesn't exist anywhere on your hard drive
            String fakeCode = "public class HelloBot { " +
                    "  public String sayHi() { " +
                    "    return \"Hello! I was compiled entirely in your RAM!\"; " +
                    "  } " +
                    "}";

            // Compile it and load it
            Class<?> compiledClass = InMemoryCompilerSandbox.compileAndLoad("HelloBot", fakeCode);

            // Create an instance of the newly compiled class and run the sayHi method!
            Object instance = compiledClass.getDeclaredConstructor().newInstance();
            Method method = compiledClass.getMethod("sayHi");

            return (String) method.invoke(instance);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}