package org.example.errortranslator;

import java.util.Scanner;

import org.example.errortranslator.analysis.ErrorTranslatorEngine;
import org.example.errortranslator.feedback.FeedbackTemplateService;
import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Debugging Test Class running the Mentor + Morality Bot architecture.
 *
 * @author Ritvik Garg (<a href="https://github.com/snivy-the-goat">snivy-the-goat</a>)
 */
public class MainDebug {
	public static void main(String[] args) {
		System.out.println("=== STARTING MENTOR & MORALITY PIPELINE SIMULATION ===");

		String mockSourceCode =
				"""
						package org.example;

						public class Calculator {
						    public int add(int a, int b) {
						        return a - b; // <-- Bug: Subtracting instead of adding
						    }
						}""";

		String mockStackTrace =
				"""
                                java.lang.AssertionError: expected:<10> but was:<0>
                                \tat org.example.CalculatorTest.testAdditionFailure(CalculatorTest.java:14)""";

		JUnitErrorContext context = new JUnitErrorContext(
				"org.example.CalculatorTest",
				"testAdditionFailure",
				mockStackTrace,
				mockSourceCode
		);

		ErrorTranslatorEngine engine = new ErrorTranslatorEngine();
		FeedbackTemplateService feedbackService = new FeedbackTemplateService();

		System.out.println("\n⏳ Running Phase 1: [error eval]...");
		String errorEvalResult = engine.runErrorEval(context);

		Scanner scanner = new Scanner(System.in);
		boolean keepLooping = true;

		System.out.println(feedbackService.getInitialPrompt(context));

		while (keepLooping) {
			System.out.println("\n✍️ Enter your explanation of what went wrong:");
			System.out.print("> ");
			String userResponse = scanner.nextLine();

			if (userResponse.trim().isEmpty()) {
				continue;
			}

			System.out.println("\n⏳ [Mentor Bot] Crafting personalized coaching response...");
			String mentorDraft = engine.generateMentorResponse(context, errorEvalResult, userResponse);

			System.out.println("⏳ [Morality Auditor] Auditing pedagogical value and rules...");
			String auditResult = engine.auditMentorResponse(mentorDraft, errorEvalResult);

			System.out.println("\n====================================================");
			System.out.println("💬 MENTOR COACHING RESPONSE:");
			System.out.println("----------------------------------------------------");
			System.out.println(mentorDraft.trim());
			System.out.println("====================================================");
			System.out.println("🛡️ MORALITY AUDIT VERDICT:");
			System.out.println("----------------------------------------------------");
			System.out.println(auditResult.trim());
			System.out.println("====================================================\n");

			System.out.println("Would you like to try again or exit? (type 'exit' to quit, or press Enter to keep debugging)");
			System.out.print("> ");
			String choice = scanner.nextLine();
			if (choice.trim().equalsIgnoreCase("exit")) {
				keepLooping = false;
			}
		}

		scanner.close();
		System.out.println("\n=== SIMULATION TERMINATED ===");
	}
}
