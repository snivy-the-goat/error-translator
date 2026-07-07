package org.example.errortranslator.analysis;

import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Analyzer responsible for dissecting a captured JUnit error context, extracting key failure metadata, and isolating the root cause line.
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class JUnitErrorAnalyzer {
	/**
	 * Extracts the specific line from the stack trace where the code failed.
	 *
	 * @param context The captured error context
	 * @return The file name and line number string in the form "{File.java}:{line_number}"
	 */
	public String extractFailureLine(JUnitErrorContext context) {
		String[] lines = context.getFullStackTrace().split("\n");

		// loop through each stack trace line to find the root test class line
		for (String line : lines) {
			// if the line contains given test class name an ignores internal JUnit classes
			if (line.contains(context.getClassName()) && line.contains(".java")) {
				int openParenthesis = line.indexOf('(');
				int closeParenthesis = line.indexOf(')');

				// if the parenthesis surround a valid file mapping signature
				if (openParenthesis != -1 && closeParenthesis != -1 && closeParenthesis > openParenthesis) {
					return line.substring(openParenthesis + 1, closeParenthesis);
				}
			}
		}

		return "Unknown Source";
	}

	/**
	 * Cleans up and isolates the human-readable assertion failer message.
	 *
	 * @param context The captured error context
	 * @return A clean description of the assertion exception statement
	 */
	public String extractFailureMessage(JUnitErrorContext context) {
		String[] lines = context.getFullStackTrace().split("\n");

		// if the stack trace array has at least one  descriptive exception line
		if (lines.length > 0) {
			String firstLine = lines[0].trim();
			int colonIndex = firstLine.indexOf(':');

			// if the exception line contains a descriptive colon separator
			if (colonIndex != -1 && colonIndex < firstLine.length() - 1) {
				return firstLine.substring(colonIndex + 1).trim();
			}
		}

		return "No message available";
	}
}
