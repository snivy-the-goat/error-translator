package org.example.errortranslator.model;

/**
 * Data container class that encapsulates the essential debugging context extracted from a failed execution
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class JUnitErrorContext {
    /** Name of the class containing the failed test */
    private final String className;
    /** Name of the test method that failed */
    private final String methodName;
    /** Complete string buffer containing the failure's stack trace */
    private final String fullStackTrace;

    /**
     * Constructor method; creates an immutable error context object containing the parsed test details
     *
     * @param className The name of the test class
     * @param methodName The name of the failed test method
     * @param fullStackTrace The complete console stack trace
     */
    public JUnitErrorContext(String className, String methodName, String fullStackTrace) {
        this.className = className;
        this.methodName = methodName;
        this.fullStackTrace = fullStackTrace;
    }

    /**
     * Retrieves the test class name
     *
     * @return The test class name
     */
    public String getClassName() { return className; }

    /**
     * Retrieves the test method name
     *
     * @return The test method name
     */
    public String getMethodName() { return methodName; }

    /**
     * Retrieves the full stack trace
     *
     * @return the raw stack trace lines
     */
    public String getFullStackTrace() { return fullStackTrace; }
}