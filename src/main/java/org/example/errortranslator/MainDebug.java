package org.example.errortranslator;

import org.example.errortranslator.interception.JUnitConsoleParserAdapter;

/**
 * Debugging Test Class
 *
 * @author Ritvik Garg
 */
public class MainDebug {
    public static void main(String[] args) {
        JUnitConsoleParserAdapter parser = new JUnitConsoleParserAdapter();

        String[] mockConsoleStream = {
                "org.example.CalculatorTest > testAdditionFailure FAILED",
                "    java.lang.AssertionError: expected:<10> but was:<7>",
                "        at org.junit.Assert.fail(Assert.java:89)",
                "        at org.example.CalculatorTest.testAdditionFailure(CalculatorTest.java:14)",
                "",
                "BUILD FAILED in 1s"
        };

        System.out.println("=== STARTING INTERCEPTION PIPELINE SIMULATION ===");
        for (String line : mockConsoleStream) {
            parser.onMockTextAvailable(line);
        }
        System.out.println("=== SIMULATION COMPLETED ===");
    }
}