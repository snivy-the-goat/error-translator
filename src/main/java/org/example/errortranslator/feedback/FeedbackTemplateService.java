package org.example.errortranslator.feedback;

import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Encapsulates all predefined user-facing prompt templates and static feedback messages.
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class FeedbackTemplateService {

	/**
	 *
	 * @param context
	 * @return
	 */
	public String getInitialPrompt(JUnitErrorContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n🚨 TEST FAILURE DETECTED 🚨\n");
		sb.append("====================================================\n");
		sb.append("Test Class:  ").append(context.className()).append("\n");
		sb.append("Test Method: ").append(context.methodName()).append("\n");
		sb.append("----------------------------------------------------\n");
		sb.append("Error Stack Trace:\n");
		sb.append(context.fullStackTrace().trim()).append("\n");
		sb.append("====================================================\n\n");
		sb.append("🤖 Look at the error output above. What do you think the issue is?");
		return sb.toString();
	}

	/**
	 * Maps a 1-4 classification grade to its predefined feedback payload.
	 *
	 * @param grade Integer grade from 1 to 4
	 * @param errorEvalResult Ground-truth error analysis (used in Grade 4 summary)
	 * @return Predefined feedback template string
	 */
	public String getFeedbackForGrade(int grade, String errorEvalResult) {
		return switch (grade) {
			case 1 -> """
					❌ [Grade 1: Unwillingness / Off-topic]
					It looks like you didn't give a complete explanation or gave up! Please take a moment to review the error message and give it a real attempt.
					❓ Question: What specific numbers or values failed in the error message?""";
			case 2 -> """
					⚠️ [Grade 2: Incorrect]
					You're putting in effort, but your explanation is pointing in the wrong direction.
					💡 Hint: Compare the expected output in the test failure against what the actual code returned.
					❓ Question: Look closely at the method being tested in your source code. What operators are it using to calculate the result?""";
			case 3 -> """
					🟡 [Grade 3: Partially Correct]
					You got part of the issue right! You identified the value mismatch, but missed where the bug actually lives in the implementation.
					💡 Hint: Check the implementation file itself, not just the test file.
					❓ Question: What exact line of code in the implementation class causes the calculation to give the wrong result?""";
			case 4 -> """
					🎉 [Grade 4: Accurate]
					Spot on! You correctly identified the root cause of the error.
					📌 Error Summary: %s
					Program completed cleanly.""".formatted(errorEvalResult != null ?
					errorEvalResult.trim() : "");
			default -> getFeedbackForGrade(1, errorEvalResult);
		};
	}
}