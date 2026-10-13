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
	/**
	 * Target endpoint for the local AI engine.
	 */
	private static final String OLLAMA_ENDPOINT = "http://localhost:11434/api/generate";
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
	 * @return JSON response string from the model
	 */
	public String query(String systemPrompt, String userPrompt) {
		String combinedPrompt = systemPrompt + "\n\nUser Input Context:\n" + userPrompt;
		String escapedPrompt = combinedPrompt.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\t", "    ")
				.replace("\n", "\\n")
				.replace("\r", "");
		String jsonPayload = "{\"model\": \"qwen2.5-coder:1.5b\",\"prompt\": \""
				+ escapedPrompt +
				"\",\"stream\": false,\"options\": {\"temperature\": 0.1}}";

		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(OLLAMA_ENDPOINT))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
					.timeout(Duration.ofSeconds(90))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return parseResponseField(response.body());
			} else {
				System.out.println(response.body());
				return "{\"error\": \"Server returned status code: " + response.statusCode() + "\"}";
			}
		} catch (IOException | InterruptedException e) {
			return "{\"error\": \"Could not establish a connection to the AI engine " +
					"backend.\"}";
		}
	}

	/**
	 * Extracts the AI's output message from the response json and unescapes the
	 * formatting to properly contain the message
	 *
	 * @param json Raw JSON response from the AI
	 * @return The formatted output message
	 */
	private String parseResponseField(String json) {
		try {
			int keyIndex = json.indexOf("\"response\":\"");
			if (keyIndex != -1) {
				int start = keyIndex + 12;
				int end = json.indexOf("\",\"", start);
				if (end == -1) {
					end = json.indexOf("\"}", start);
				}
				if (end != -1) {
					String rawOutput = json.substring(start, end);
					return rawOutput.replace("\\n", "\n")
							.replace("\\\"", "\"")
							.replace("\\\\", "\\");
				}
			}
			return json;
		} catch (Exception e) {
			return json;
		}
	}
}
