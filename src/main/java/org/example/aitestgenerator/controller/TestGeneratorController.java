package org.example.aitestgenerator.controller;

import org.example.aitestgenerator.compiler.InMemoryCompilerSandbox;
import org.example.aitestgenerator.compiler.TestExecutionEngine;
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
            // A raw string containing a real JUnit 5 test class
            String fakeTestCode =
                    "import org.junit.jupiter.api.Test;\n" +
                            "import static org.junit.jupiter.api.Assertions.*;\n" +
                            "public class MathSandboxTest {\n" +
                            "    @Test\n" +
                            "    public void testAddition() {\n" +
                            "        assertEquals(4, 2 + 2);\n" +
                            "    }\n" +
                            "    @Test\n" +
                            "    public void testFailure() {\n" +
                            "        assertEquals(5, 2 + 2, \"Math is broken!\");\n" +
                            "    }\n" +
                            "}";

            // 1. Compile the string into RAM
            Class<?> compiledClass = InMemoryCompilerSandbox.compileAndLoad("MathSandboxTest", fakeTestCode);

            // 2. Run the newly compiled class through JUnit
            return TestExecutionEngine.runTestClass(compiledClass);

        } catch (Exception e) {
            return "Compilation Error: " + e.getMessage();
        }
    }
    @PostMapping("/generate-and-run")
    public String generateCompileAndRun(@RequestBody TestRequest request) {
        return testGenerationService.generateCompileAndRunTest(request);
    }
}