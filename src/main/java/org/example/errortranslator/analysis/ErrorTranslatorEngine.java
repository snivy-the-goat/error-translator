package org.example.errortranslator.analysis;

import org.example.errortranslator.model.JUnitErrorContext;
import org.jetbrains.annotations.NotNull;

/**
 * Processing engine that converts raw exception signatures into human-readable debugging translations and actionable next steps.
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class ErrorTranslatorEngine {
	/** Utility analyzer used to pull structural token out of the raw text block */
	private final JUnitErrorAnalyzer analyzer = new JUnitErrorAnalyzer();

	/**
	 * Generates a structural, user-friendly debugging card text out of the raw exception data.
	 *
	 * @param context The fully-populated error context container
	 * @return A formatted translation string ready for UI injection
	 */
	public String generateTranslationCard(JUnitErrorContext context) {
		String failureLine = analyzer.extractFailureLine(context);
		String cleanMessage = analyzer.extractFailureMessage(context);
		String exceptionType = getExceptionType(context);

		StringBuilder card = new StringBuilder();
		card.append("====================================================\n");
		card.append("💡 ERROR TRANSLATOR INTERPRETATION\n");
		card.append("====================================================\n");
		card.append("❌ Failure Location : ").append(context.getClassName()).append(".").append(context.getMethodName()).append("()\n");
		card.append("📍 Exact Code Line : ").append(failureLine).append("\n");
		card.append("⚠️ Exception Type   : ").append(exceptionType).append("\n");
		card.append("📝 Plain English    : The test expected a specific value or state, but received something else instead.\n");
		card.append("🔍 Details          : ").append(cleanMessage).append("\n");
		card.append("----------------------------------------------------\n");
		card.append("🛠️ SUGGESTED FIX:\n");

		// evaluate what type fix blueprint matches the exception type string
		if (exceptionType.contains("AssertionError")) {
			card.append("  -> Check the logic modifying the variable on line [").append(failureLine).append("].\n");
			card.append("  -> Verify if your mock assertions or business rules changed recently.\n");
		} else if (exceptionType.contains("NullPointerException")) {
			card.append("  -> An object reference accessed on line [").append(failureLine).append("] is null.\n");
			card.append("  -> Add a null check or ensure your dependencies are properly instantiated.\n");
		} else {
			card.append("  -> Inspect the conditions leading up to line [").append(failureLine).append("] to check resource state.\n");
		}
		card.append("====================================================");

		return card.toString();
	}

	private static @NotNull String getExceptionType(JUnitErrorContext context) {
		String exceptionType = "UnknownException";

		String[] lines = context.getFullStackTrace().split("\n");

		// check if the trace text buffer contains an explicit exception signature line
		if (lines.length > 0) {
			String firstLine = lines[0].trim();
			int colonIndex = firstLine.indexOf(':');

			// extract the exception class type prefix if a descriptive colon exists
			if (colonIndex != -1) {
				exceptionType = firstLine.substring(0, colonIndex).trim();
			} else {
				exceptionType = firstLine;
			}
		}
		return exceptionType;
	}
}
