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
		String systemPrompt = """
				You are a precise automated code execution analyzer.
				Task: Review the provided JUnit stack trace failure and source code.
				Output exactly what the issue is, the line number, and the core mistake.""";

		String userPrompt = "=== FAILED TEST STACK TRACE ===\n" + context.fullStackTrace() + "\n\n" +
				"=== IMPLEMENTATION SOURCE CODE ===\n" + context.sourceCode();

		return aiClient.query(systemPrompt, userPrompt, 0.0);
	}

	/**
	 * Generates a supportive, Socratic coaching response based on the student's
	 * attempt.
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
				2. SECOND, ask a single guiding question directing them to look at expected vs actual values or the method signature.

				ABSOLUTE PROHIBITIONS:
				- NEVER write code blocks (no ```).
				- NEVER provide the direct solution or write out fixed code.""";

		String userPrompt = "=== GROUND TRUTH ERROR ===\n" + errorEval + "\n\n" +
				"=== SOURCE CODE ===\n" + context.sourceCode() + "\n\n" +
				"=== STUDENT'S GUESS ===\n\"" + studentResponse + "\"";

		return aiClient.query(systemPrompt, userPrompt, 0.3);
	}

	/**
	 * Generates category scores using strict behavioral anchors.
	 *
	 * @param mentorDraft The draft produced by the mentor bot.
	 * @param errorEval   The ground truth error evaluation.
	 * @return Raw scoring string.
	 */
	public String evaluateScores(String mentorDraft, String errorEval) {
		String systemPrompt = """
				You are a precise educational metrics evaluator.
				Evaluate the mentor draft against these 6 categories on a strict 1 to 5 scale:

				1. Directness (Anti-Spoiler):
				   - 5: Purely Socratic (questions only, NO code blocks, NO exact answers).
				   - 3-4: Directional hint pointing to a line or concept.
				   - 1-2: SPOILER (contains code blocks ```, replacement snippets, or explicit answers).
				2. Tone: 5=Warm & normalizes confusion, 3=Neutral, 1=Condescending.
				3. Relevance: 5=Directly addresses student state, 3=Broadly relevant, 1=Off-topic.
				4. Accuracy: 5=Factually true context, 3=Passable, 1=Factually incorrect.
				5. Scope: 5=Asks ONE single question or focus point, 3=Two questions, 1=Overwhelming/wall of text.
				6. Construction: 5=Acknowledges student input BEFORE asking a question, 1=Ignores input / abrupt pivot.

				STRICT OUTPUT FORMAT:
				SCORES: Directness[X], Tone[X], Relevance[X], Accuracy[X], Scope[X], Construction[X]""";

		String userPrompt = "=== GROUND TRUTH ===\n" + errorEval + "\n\n=== MENTOR DRAFT ===\n" + mentorDraft;
		return aiClient.query(systemPrompt, userPrompt, 0.0);
	}

	/**
	 * Synthesizes a qualitative critique focused on the lowest score.
	 *
	 * @param rawScores   The scoring metrics.
	 * @param mentorDraft The mentor draft being evaluated.
	 * @return A 1-sentence qualitative critique.
	 */
	public String synthesizeCritique(String rawScores, String mentorDraft) {
		String systemPrompt = """
				You are an educational compliance officer.
				Task: Read the provided category scores and mentor draft. Write a concise 1-sentence critique explaining the primary weakness of the response.

				CRITIQUE GUIDELINES:
				- Base the critique strictly on the category with the lowest score.
				- NEVER mention score numbers or thresholds in the sentence.
				- NEVER criticize the mentor for withholding code or asking a question (withholding code is required).

				OUTPUT FORMAT:
				CRITIQUE: [1 sentence explanation]""";

		String userPrompt = "=== SCORES ===\n" + rawScores + "\n\n=== MENTOR DRAFT ===\n" + mentorDraft;
		return aiClient.query(systemPrompt, userPrompt, 0.0);
	}

	/**
	 * Iterative Pipeline Loop: Generates a mentor response, audits it, and if it
	 * fails, feeds the critique back to the mentor to revise up to a maximum number
	 * of attempts.
	 *
	 * @param context         The test error context.
	 * @param errorEval       The ground truth error evaluation.
	 * @param studentResponse The student input.
	 * @return The final passing result package, or a clean fallback error if all
	 *         attempts fail.
	 */
	public String auditMentorResponse(JUnitErrorContext context, String errorEval, String studentResponse) {
		int maxAttempts = 3;
		String mentorDraft = generateMentorResponse(context, errorEval, studentResponse);

		if (mentorDraft.startsWith("ERROR:")) {
			return mentorDraft;
		}

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			String rawScores = evaluateScores(mentorDraft, errorEval);
			if (rawScores.startsWith("ERROR:")) {
				return rawScores;
			}

			String critiqueBlock = synthesizeCritique(rawScores, mentorDraft);
			if (critiqueBlock.startsWith("ERROR:")) {
				return critiqueBlock;
			}

			boolean pass = !rawScores.contains("Directness[1]") &&
					!rawScores.contains("Directness[2]") &&
					!rawScores.contains("Directness[3]") &&
					!rawScores.matches(".*(Tone|Relevance|Accuracy|Scope|Construction)\\[[12]\\].*");

			if (pass) {
				return "VERDICT: PASS\n" + rawScores + "\n" + critiqueBlock + "\n\n" + mentorDraft.trim();
			}

			if (attempt < maxAttempts) {
				String revisionPrompt = "Your previous mentoring draft failed the educational morality audit with this feedback:\n"
						+
						critiqueBlock
						+ "\n\nPlease rewrite your Socratic response to resolve this weakness while maintaining a warm, single-question structure.";

				mentorDraft = aiClient.query(
						"You are an expert programming mentor refining your previous coaching draft.",
						revisionPrompt,
						0.3);

				if (mentorDraft.startsWith("ERROR:")) {
					return mentorDraft;
				}
			}
		}

		return "ERROR: An error has occurred, please retry your input.";
	}
}
