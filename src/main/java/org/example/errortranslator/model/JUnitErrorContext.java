package org.example.errortranslator.model;

/**
 * Data container class that encapsulates the essential debugging context extracted from a failed execution
 *
 * @param className      Name of the class containing the failed test.
 * @param methodName     Name of the test method that failed.
 * @param fullStackTrace Complete string buffer containing the failure's stack trace.
 * @author Ritvik Garg (<a href="https://github.com/snivy-the-goat">snivy-the-goat</a>)
 */
public record JUnitErrorContext(String className, String methodName,
                                String fullStackTrace, String sourceCode) {
	/**
	 * Constructor method; creates an immutable error context object containing the parsed test details.
	 *
	 * @param className      The name of the test class
	 * @param methodName     The name of the failed test method
	 * @param fullStackTrace The complete console stack trace
	 */
	public JUnitErrorContext {}

	/**
	 * Retrieves the test class name.
	 *
	 * @return The test class name
	 */
	@Override
	public String className() {return className;}

	/**
	 * Retrieves the test method name.
	 *
	 * @return The test method name
	 */
	@Override
	public String methodName() {return methodName;}

	/**
	 * Retrieves the full stack trace.
	 *
	 * @return The raw stack trace lines
	 */
	@Override
	public String fullStackTrace() {return fullStackTrace;}

	/**
	 * Retrieves the source code.
	 *
	 * @return The source code
	 */
	@Override
	public String sourceCode() {return sourceCode;}
}
