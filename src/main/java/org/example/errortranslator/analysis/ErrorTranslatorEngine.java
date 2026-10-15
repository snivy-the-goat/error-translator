package org.example.errortranslator.analysis;

import org.example.errortranslator.client.AIClient;
import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Pedagogical multi-agent engine that generates custom guidance and audits it
 * using a strict teaching morality rubric.
 *
 * @author Ritvik Garg
 *         (<a href="https://github.com/snivy-the-goat">snivy-the-goat</a>)
 */
public class ErrorTranslatorEngine {
	/** AI Handler */
	private final AIClient aiClient = new AIClient();

	/**
	 * Asks the AI Client to evaluate what caused an error in source code
	 *
	 * @param context An object containing the stack trace and the source code to
	 *                analyze
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

		return aiClient.query(systemPrompt, userPrompt, 0.1);
	}

	/**
	 *
	 * @param context      An object that bundles the stack trace and source code
	 * @param errorEval    The evaluation from the AI
	 * @param userResponse The answer provided by the user
	 * @return The advice/help the AI recommends giving to the user to help their
	 *         understanding
	 */
	public String generateMentorResponse(JUnitErrorContext context, String errorEval, String studentResponse) {
		String systemPrompt = """
				You are an encouraging, expert programming mentor.
				Goal: Help the student understand the error through Socratic guidance.

				STRUCTURE RULES:
				1. FIRST, acknowledge the student's input (if they are stuck or have no idea, normalize it; if they made a guess, critique or validate it constructively).
				2. SECOND, ask a single guiding question directing them to look at the expected vs actual values or the method signature.

				ABSOLUTE PROHIBITIONS:
				- NEVER write code blocks (no ```).
				- NEVER provide the direct solution or write out fixed code.""";

		String userPrompt = "=== GROUND TRUTH ERROR ===\n" + errorEval + "\n\n" +
				"=== SOURCE CODE ===\n" + context.sourceCode() + "\n\n" +
				"=== STUDENT'S GUESS ===\n\"" + studentResponse + "\"";

		return aiClient.query(systemPrompt, userPrompt, 0.3);
	}

	/**
	 *
	 * @param mentorDraft
	 * @param errorEval
	 * @return
	 */
	public String auditMentorResponse(String mentorDraft, String errorEval) {
		String systemPrompt = """
				You are a strict educational metrics evaluator and compliance arbiter.
				Evaluate the mentor draft against these 6 categories on a strict 1 to 5 scale:

				1. Directness (Anti-Spoiler): 5=Purely Socratic/No code, 4=Directional hint, 3=Subtle hint, 2=Partial leak, 1=Total spoiler/Code block.
				2. Tone (Emotional Safety): 5=Warm & normalizes confusion, 4=Supportive, 3=Neutral, 2=Dry, 1=Condescending/Dismissive of feelings.
				3. Relevance (Context Alignment): 5=Directly addresses student state, 4=Aligned, 3=Broadly relevant, 2=Tangential, 1=Off-topic.
				4. Accuracy (Conceptual Soundness): 5=Flawlessly aligned with ground truth, 4=Mostly accurate, 3=Passable, 2=Misleading, 1=Factually incorrect.
				5. Scope (Focus / One Thing): 5=Laser-focused on one idea, 4=Well-scoped, 3=Multi-focus, 2=Overwhelming, 1=Cognitive overload.
				6. Construction (Pedagogical Bridge): 5=Bridges student input to guidance smoothly, 4=Good connection, 3=Basic, 2=Abrupt pivot, 1=Invalidating.

				Rules for Verdict:
				- PASS: If Directness >= 4 AND all other categories are >= 3.
				- FAIL: If any category is <= 2 OR Directness <= 3.

				CRITIQUE RULES:
				- Write a concise 1-sentence qualitative critique explaining the actual pedagogical flaw based on the lowest score.
				- If Directness <= 3: Mention over-spoiling or giving away too much code/hints.
				- If Accuracy <= 2: Mention misleading or incorrect technical direction.
				- If Relevance <= 2: Mention failing to address or align with what the student said.
				- If Scope <= 2: Mention asking too many questions or losing focus on one concept.
				- If Tone <= 2: Mention unsupportive or harsh language.
				- If Construction <= 2: Mention an abrupt pivot without bridging the student's input.
				- STRICT CONSTRAINT: Never mention score numbers (like 'score of 2'). Never criticize the mentor for withholding code or not explaining the solution (withholding code is correct).

				STRICT OUTPUT FORMAT:
				VERDICT: [PASS or FAIL]
				SCORES: Directness[X], Tone[X], Relevance[X], Accuracy[X], Scope[X], Construction[X]
				CRITIQUE: [1 sentence explanation]""";

		String userPrompt = "=== GROUND TRUTH ===\n" + errorEval + "\n\n=== MENTOR DRAFT ===\n" + mentorDraft;
		return aiClient.query(systemPrompt, userPrompt, 0.0);
	}
}
