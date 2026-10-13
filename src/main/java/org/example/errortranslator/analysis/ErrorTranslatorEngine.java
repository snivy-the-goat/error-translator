package org.example.errortranslator.analysis;

import org.example.errortranslator.client.AIClient;
import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Pedagogical multi-agent engine that generates custom guidance and audits it
 * using a strict teaching morality rubric.
 *
 * @author Ritvik Garg (<a href="https://github.com/snivy-the-goat">snivy-the-goat</a>)
 */
public class ErrorTranslatorEngine {
	/** AI Handler */
	private final AIClient aiClient = new AIClient();

	/**
	 * Asks the AI Client to evaluate what caused an error in source code
	 * @param context An object containing the stack trace and the source code to analyze
	 * @return The AI's evaluation of what the error is
	 */
	public String runErrorEval(JUnitErrorContext context) {
		String systemPrompt;
            systemPrompt = """
                           You are a precise automated code execution analyzer.
                           Task: Review the provided JUnit stack trace failure and source code.
                           Output exactly what the issue is, the line number, and the core mistake.""";

		String userPrompt = "=== FAILED TEST STACK TRACE ===\n" + context.fullStackTrace() + "\n\n" +
				"=== IMPLEMENTATION SOURCE CODE ===\n" + context.sourceCode();

		return aiClient.query(systemPrompt, userPrompt);
	}

	/**
	 *
	 * @param context An object that bundles the stack trace and source code
	 * @param errorEval The evaluation from the AI
	 * @param userResponse The answer provided by the user
	 * @return The advice/help the AI recommends giving to the user to help their understanding
	 */
	public String generateMentorResponse(JUnitErrorContext context, String errorEval, String userResponse) {
		String systemPrompt = """
                                      You are an encouraging, expert programming mentor.
                                      Goal: Write a response to help the user understand the error and figure it out themselves.
                                      CRITICAL RULES:
                                      - DO NOT give them the direct answer or write the fixed code snippet.
                                      - Acknowledge their effort and current explanation.
                                      - Point them toward the correct file, method, or logic discrepancy using a guiding question.""";

		String userPrompt = "=== GROUND TRUTH ERROR ===\n" + errorEval + "\n\n" +
				"=== SOURCE CODE ===\n" + context.sourceCode() + "\n\n" +
				"=== user'S GUESS ===\n\"" + userResponse + "\"";

		return aiClient.query(systemPrompt, userPrompt);
	}

	public String auditMentorResponse(String mentorDraft, String errorEval) {
		String systemPrompt = """
                                      You are a strict Pedagogical Morality Auditor reviewing an AI mentor's draft.
                                      Evaluate the draft against this Teaching Rubric:

                                      PEDAGOGICAL RUBRIC:
                                      - PASS: The response successfully guides the user, encourages learning, points out where to look, AND crucially DOES NOT reveal the explicit code fix or direct answer.
                                      - FAIL_GAVE_ANSWER: The response spoils the solution by showing fixed code or directly stating the exact code change needed.
                                      - FAIL_DISCOURAGING: The response is overly harsh, cynical, or unhelpful.

                                      STRICT OUTPUT FORMAT:
                                      VERDICT: [PASS or FAIL_GAVE_ANSWER or FAIL_DISCOURAGING]
                                      FEEDBACK: [1 sentence explaining why, or suggestions to rewrite if failed]""";

		String userPrompt = "=== GROUND TRUTH ERROR ===\n" + errorEval + "\n\n" +
				"=== MENTOR DRAFT TO AUDIT ===\n" + mentorDraft;

		return aiClient.query(systemPrompt, userPrompt);
	}
}
