package org.example.errortranslator.interception;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import org.example.errortranslator.model.JUnitErrorContext;
import org.jetbrains.annotations.NotNull;

/**
 * State-machine console parser that filters real-time standard output stream to intercept, capture, and package JUnit test execution failures.
 *
 * @author Ritvik Garg
 */
public class JUnitConsoleParserAdapter extends ProcessAdapter {

    /** Whether the parser is actively buffering stack trace lines. */
    private boolean isRecording = false;

    /** The name of the class currently failing. */
    private String currentClassName = "";

    /** The name of the method currently failing. */
    private String currentMethodName = "";

    /** The multi-line error stack trace. */
    private final StringBuilder stackTraceBuffer = new StringBuilder();

    /**
     * IDE hook that automatically intercepts console output within the IntelliJ environment.
     *
     * @param event The container holding the line of console text
     * @param outputType The type of process output stream (stdout, stderr, system)
     */
    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String line = event.getText();

        // skip further processing if the intercepted line holds no content
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String cleanLine = line.trim();

        // if the state machine is currently looking for a brand-new test failure header
        if (!isRecording) {
            // if the line matches the standard JUnit failure signature
            if (cleanLine.contains(" > ") && cleanLine.contains(" FAILED")) {
                isRecording = true;
                stackTraceBuffer.setLength(0);
                parseTestHeader(cleanLine);
            }
        } else {
            // if the current line is part of a Java stack trace pattern
            if (cleanLine.startsWith("at ") || cleanLine.contains("Exception") || cleanLine.contains("Error")) {
                stackTraceBuffer.append(line).append("\n");
            } else {
                JUnitErrorContext context = new JUnitErrorContext(
                        currentClassName,
                        currentMethodName,
                        stackTraceBuffer.toString()
                );

                triggerProcessingPipeline(context);

                isRecording = false;
                currentClassName = "";
                currentMethodName = "";
            }
        }
    }

    /**
     * Isolates and extracts individual test structure names out of a raw failure header text.
     *
     * @param headerLine The raw text header indicating a test failure
     */
    private void parseTestHeader(String headerLine) {
        try {
            String cleanLine = headerLine.replace(" FAILED", "").trim();
            String[] parts = cleanLine.split(" > ");

            // if the split pattern split the header clean into a class element and method element
            if (parts.length == 2) {
                this.currentClassName = parts[0].trim();
                this.currentMethodName = parts[1].trim();
            } else {
                // fallback
                this.currentClassName = "UnknownClass";
                this.currentMethodName = "UnknownMethod";
            }
        } catch (Exception e) {
            // fallback
            this.currentClassName = "ParsingError";
            this.currentMethodName = "ParsingError";
        }
    }

    /**
     * Submits the extracted metadata to downstream processing modules for diagnostics.
     *
     * @param context The fully-populated error context container
     */
    private void triggerProcessingPipeline(JUnitErrorContext context) {
        System.out.println("!!! ERROR GRABBED SUCCESSFULLY !!!");
        System.out.println("Class: " + context.getClassName());
        System.out.println("Method: " + context.getMethodName());
        System.out.println("Trace Length: " + context.getFullStackTrace().length());
    }

    /**
     * Legacy diagnostic execution harness designed to explicitly replicate mock lines inside offline local environments.<br>
     * Used to test functionality; now deprecated.
     *
     * @param rawLine Mock terminal output line entry
     */
    public void onMockTextAvailable(String rawLine) {
        // skip line verification if the simulated raw input line contains no characters
        if (rawLine == null || rawLine.trim().isEmpty()) {
            return;
        }

        String cleanLine = rawLine.trim();

        // if the mock state machine is currently looking for a brand-new test failure header
        if (!isRecording) {
            // if the line matches the standard JUnit failure signature
            if (cleanLine.contains(" > ") && cleanLine.contains(" FAILED")) {
                isRecording = true;
                stackTraceBuffer.setLength(0);
                parseTestHeader(cleanLine);
            }
        } else {
            // whether the mock string content belongs inside the trace array boundaries
            if (cleanLine.startsWith("at ") || cleanLine.contains("Exception") || cleanLine.contains("Error")) {
                stackTraceBuffer.append(rawLine).append("\n");
            } else {
                // combines compiled metrics into context object upon loop termination
                JUnitErrorContext context = new JUnitErrorContext(
                        currentClassName,
                        currentMethodName,
                        stackTraceBuffer.toString()
                );
                triggerProcessingPipeline(context);
                isRecording = false;
                currentClassName = "";
                currentMethodName = "";
            }
        }
    }
}