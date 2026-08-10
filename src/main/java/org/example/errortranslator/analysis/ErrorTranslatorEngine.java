package org.example.errortranslator.analysis;

import org.example.errortranslator.client.AIClient;
import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Processing engine that converts raw exception signatures into human-readable debugging translations and actionable next steps.
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class ErrorTranslatorEngine {
	/**
	 * Client to connect and send prompts to the AI.
	 */
	private final AIClient aiClient = new AIClient();

	/**
	 *
	 * @param context
	 * @return
	 */
	public String runErrorEval(JUnitErrorContext context) {
		/* String systemPrompt = """
				You are a precise, automated code execution analyzer.
				Task: Review the provided JUnit stack trace failure and the implementation source code.
				Output exactly what the issue is in the code. State the line number, what the code is doing wrong, \
				and what the fix should be. Be hyper-specific to the logic present in the source files.""";

		 */
		String systemPrompt = """
				You are a precise automated code execution analyzer.
				Task: Review the provided JUnit stack trace failure and the implementation source code.
				Output exactly what the issue is in the code. State the line number, what the code is doing wrong, \
				and what the fix should be. Be hyper-specific to the logic present in the source files.""";

		String userPrompt = "=== FAILED TEST STACK TRACE ===\n" + context.fullStackTrace() + "\n" +
				"\n" +
				"=== IMPLEMENTATION SOURCE CODE ===\n" + context.sourceCode();

		return aiClient.query(systemPrompt, userPrompt);
	}

	/**
	 *
	 * @param studentResponse
	 * @param errorEval
	 * @return
	 */
	public String gradeStudentResponse(String studentResponse, String errorEval) {
		/* String systemPrompt = """
				You are an objective grading classifier.
				Compare the Student Response against the provided 'Error Eval' truth and assign a score from 1 to 4:
				
				1 = Completely Wrong (Unwillingness to try, gibberish, 'idk', or lazy response)
				2 = Incorrect (Off-base, but putting in visible effort)
				3 = Partially Correct (Identifies some aspects right, but misses or misinterprets other key details)
				4 = Accurate (Almost completely or fully correct explanation of the bug)
				
				CRITICAL: Output ONLY a single digit integer (1, 2, 3, or 4). Do NOT output any words, labels, or extra text.""";
		*/

		String systemPrompt = """
				You are an automated grading engine evaluating a student's bug diagnosis.
				Compare the Student Response against the Ground-Truth Error Eval and assign a score from 1 to 4:
				
				RUBRIC DEFINITIONS:
				1 = Completely Wrong: Unwilling to try, off-topic, gibberish, or gives up (e.g., 'idk', 'i give up').
				2 = Incorrect: Points to an ENTIRELY WRONG root cause, wrong file, or unrelated issue (e.g., blaming memory limits, network errors, or the test file).
				3 = Partially Correct: Correctly identifies WHICH function/method is failing or states THAT the function isn't working, BUT misses the exact code mechanism or line-level detail.
				4 = Accurate: Fully or almost fully explains the exact bug (e.g., 'the add method uses subtraction instead of addition').
				
				STRICT OUTPUT RULES:
				- Line 1 MUST start with 'GRADE: ' followed by a single integer (1, 2, 3, or 4).
				- Line 2 MUST start with 'EXPLANATION: ' followed by 1 sentence explaining why this grade was chosen.
				- DO NOT write Java code snippets.
				- DO NOT address or teach the student directly.""";
		String userPrompt = "=== ERROR EVAL TRUTH ===\n" + errorEval + "\n\n" +
				"=== STUDENT RESPONSE ===\n" + studentResponse;

		String rawResponse = aiClient.query(systemPrompt, userPrompt);
		return rawResponse;
	}

	/**
	 *
	 * @param rawOutput
	 * @return
	 */
	public int parseGrade(String rawOutput) {
		if (rawOutput == null) return 0;

		for (char c : rawOutput.toCharArray()) {
			if (c >= '1' && c <= '4') {
				return Character.getNumericValue(c);
			}
		}
		return 0;
	}
}
