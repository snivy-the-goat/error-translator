package org.example.errortranslator.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Handles HTTP communication with the AI server backend.
 * Handles request execution Java 11+ HttpClient.
 *
 * @author Ritvik Garg
 *         (<a href="https://github.com/snivy-the-goat">snivy-the-goat</a>)
 */
public class AIClient {
	/** API Key to access the AI server */
	private static final String API_KEY = "AQ.Ab8RN6JtJ9mxSVrJdsC74kURcTok_VS4dWm6NlZ6YatTHhyocQ";
	/** URL to the AI server, including API Key */
	private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key="
			+ API_KEY;
	/**
	 * Configured HTTP client with timeout rules.
	 */
	private final HttpClient httpClient;

	/**
	 * Constructor method; creates a client to connect and converse with the AI.
	 */
	public AIClient() {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(20))
				.build();
	}

	/**
	 * Sends a prompt payload to the AI engine and returns a response block.
	 *
	 * @param systemPrompt Instructions containing the AI's persona and restrictions
	 * @param userPrompt   The context or user input to evaluate
	 * @param temperature  The creativity of the model's responses
	 * @return JSON response string from the model
	 */
	public String query(String systemPrompt, String userPrompt, double temperature) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                String jsonPayload = String.format("""
                        {
                          "system_instruction": {
                            "parts": [{"text": "%s"}]
                          },
                          "contents": [{
                            "parts": [{"text": "%s"}]
                          }],
                          "generationConfig": {
                            "temperature": %.2f
                          }
                        }
                        """, escapeJson(systemPrompt), escapeJson(userPrompt), temperature);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(90))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Handle temporary 503 server overload with exponential backoff retry
                if (response.statusCode() == 503 && attempt < maxRetries - 1) {
                    attempt++;
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                if (response.statusCode() == 503) {
                    return "ERROR: The server is currently experiencing high demand. Please try again later.";
                }

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body());
                } else {
                    return "ERROR: An unexpected error occurred while communicating with the AI service. Code: " + response.statusCode() + ". Please try again later.";
                }

            } catch (IOException | InterruptedException e) {
                if (attempt == maxRetries - 1) {
                    return "ERROR: Could not establish a connection to the AI engine backend. Please check your network.";
                }
            }
            attempt++;
        }

        return "ERROR: The server is currently experiencing high demand. Please try again later.";
    }

	/**
	 * Extracts the text response from Gemini's JSON structure.
	 *
	 * @param jsonResponseBody Raw JSON response from Gemini
	 * @return The formatted output message
	 */
	private String parseGeminiResponse(String jsonResponseBody) {
		try {
			int textMarker = jsonResponseBody.indexOf("\"text\": \"");
			if (textMarker == -1)
				return jsonResponseBody;

			int startIndex = textMarker + 9;
			int endIndex = startIndex;

			boolean escaped = false;
			while (endIndex < jsonResponseBody.length()) {
				char c = jsonResponseBody.charAt(endIndex);
				if (c == '\\' && !escaped) {
					escaped = true;
				} else if (c == '"' && !escaped) {
					break;
				} else {
					escaped = false;
				}
				endIndex++;
			}

			String rawText = jsonResponseBody.substring(startIndex, endIndex);
			return rawText.replace("\\n", "\n")
					.replace("\\\"", "\"")
					.replace("\\\\", "\\");
		} catch (Exception e) {
			return jsonResponseBody;
		}
	}

	/**
	 * Escapes special characters for JSON formatting.
	 *
	 * @param input Formatted output message
	 * @return Formatted output message wih escaping
	 */
	private String escapeJson(String input) {
		return input.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
