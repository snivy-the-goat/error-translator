package org.example.errortranslator;

import org.example.errortranslator.analysis.ErrorTranslatorEngine;
import org.example.errortranslator.feedback.FeedbackTemplateService;
import org.example.errortranslator.model.JUnitErrorContext;

import java.util.Scanner;

/**
 * Debugging Test Class
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class MainDebug {
	public static void main(String[] args) {
		System.out.println("=== STARTING PREDEFINED FEEDBACK PIPELINE SIMULATION ===");

		// Mock failure state setup
		String mockSourceCode =
				"""
						package org.example;
						
						public class Calculator {
						    public int add(int a, int b) {
						        return a - b; // <-- Bug: Subtracting instead of adding
						    }
						}""";

		String mockStackTrace =
				"java.lang.AssertionError: expected:<10> but was:<0>\n" +
						"\tat org.example.CalculatorTest.testAdditionFailure(CalculatorTest.java:14)";

		JUnitErrorContext context = new JUnitErrorContext(
				"org.example.CalculatorTest",
				"testAdditionFailure",
				mockStackTrace,
				mockSourceCode
		);

		ErrorTranslatorEngine engine = new ErrorTranslatorEngine();
		FeedbackTemplateService feedbackService = new FeedbackTemplateService();

		// 1. Run Phase 1: Error Eval
		System.out.println("\n⏳ Running Phase 1: [error eval]...");
		String errorEvalResult = engine.runErrorEval(context);

		System.out.println("\n====================================================");
		System.out.println("🤖 AI GROUND TRUTH ERROR EVALUATION:");
		System.out.println("====================================================");
		System.out.println(errorEvalResult.trim());
		System.out.println("====================================================\n");

		// 2. Interactive Loop
		Scanner scanner = new Scanner(System.in);
		boolean keepLooping = true;

		System.out.println("\n" + feedbackService.getInitialPrompt(context));

		while (keepLooping) {
			System.out.println("\n✍️ Enter your explanation:");
			System.out.print("> ");
			String studentResponse = scanner.nextLine();

			if (studentResponse.trim().isEmpty()) {
				continue;
			}

			System.out.println("\n⏳ Classifying response...");
			String rawAiEvaluation =
					engine.gradeStudentResponse(studentResponse, errorEvalResult);
			int grade = engine.parseGrade(rawAiEvaluation);

			System.out.println("\n====================================================");
			System.out.println("🧠 AI CLASSIFICATION REASONING:");
			System.out.println("----------------------------------------------------");
			System.out.println(rawAiEvaluation.trim());
			System.out.println("====================================================\n");
			System.out.println(feedbackService.getFeedbackForGrade(grade,
					 errorEvalResult));
			System.out.println("====================================================");

			if (grade == 4) {
				keepLooping = false;
			}
		}

		scanner.close();
		System.out.println("\n=== SIMULATION TERMINATED ===");
	}
}