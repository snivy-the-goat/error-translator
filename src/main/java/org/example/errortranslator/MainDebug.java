package org.example.errortranslator;

import org.example.errortranslator.analysis.ErrorTranslatorEngine;
import org.example.errortranslator.model.JUnitErrorContext;

/**
 * Debugging Test Class
 *
 * @author Ritvik Garg (<a href="https://github.com/the-goat-man">the-goat-man</a>)
 */
public class MainDebug {
    public static void main(String[] args) {
        System.out.println("=== STARTING INTERCEPTION PIPELINE SIMULATION ===");

        // simulate a raw multi-line terminal dump from a failed JUnit test
        String mockClassName = "org.example.CalculatorTest";
        String mockMethodName = "testAdditionFailure";

        String mockStackTrace =
                """
						java.lang.AssertionError: expected:<10> but was:<7>
						\tat org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
						\tat org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
						\tat org.junit.jupiter.api.AssertEquals.failNotEqual(AssertEquals.java:197)
						\tat org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:150)
						\tat org.junit.jupiter.api.Assertions.assertEquals(Assertions.assertEquals.java:563)
						\tat org.example.CalculatorTest.testAdditionFailure(CalculatorTest.java:14)
						\tat java.base/java.util.ArrayList.forEach(ArrayList.java:1596)""";

        JUnitErrorContext context = new JUnitErrorContext(mockClassName, mockMethodName, mockStackTrace);

        ErrorTranslatorEngine translator = new ErrorTranslatorEngine();
        String translationCard = translator.generateTranslationCard(context);

        System.out.println(translationCard);

        System.out.println("\n=== SIMULATION COMPLETED ===");
    }
}