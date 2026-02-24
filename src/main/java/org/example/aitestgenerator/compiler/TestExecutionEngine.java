package org.example.aitestgenerator.compiler;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestExecutionEngine {

    public static String runTestClass(Class<?> testClass) {
        // 1. Tell JUnit which class we want to run
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();

        // 2. Create the JUnit Launcher (the engine)
        Launcher launcher = LauncherFactory.create();

        // 3. Create a listener to record the results (Pass/Fail/Errors)
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // 4. Execute the tests!
        launcher.execute(request);

        // 5. Gather the results and format them into a readable report
        TestExecutionSummary summary = listener.getSummary();
        return formatReport(summary, testClass.getSimpleName());
    }

    private static String formatReport(TestExecutionSummary summary, String className) {
        StringBuilder report = new StringBuilder();
        report.append("--- Test Execution Report for ").append(className).append(" ---\n");
        report.append("Tests Found: ").append(summary.getTestsFoundCount()).append("\n");
        report.append("Tests Succeeded: ").append(summary.getTestsSucceededCount()).append("\n");
        report.append("Tests Failed: ").append(summary.getTestsFailedCount()).append("\n");

        if (summary.getTestsFailedCount() > 0) {
            report.append("\nFailure Details:\n");
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                report.append("- ").append(failure.getTestIdentifier().getDisplayName()).append(" failed:\n");
                report.append("  Reason: ").append(failure.getException().getMessage()).append("\n");

                // Optional: Print stack trace to a string if you want deep debugging
                StringWriter sw = new StringWriter();
                failure.getException().printStackTrace(new PrintWriter(sw));
                // report.append(sw.toString()).append("\n");
            }
        }
        return report.toString();
    }
}