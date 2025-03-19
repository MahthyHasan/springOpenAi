package com.Task.Hasan.Services;

import com.Task.Hasan.Entity.Requests;
import com.Task.Hasan.Repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Service
public class OpenAiServices {
//    @Value("${openai.api.key}")
//    private String openAiApiKey;
//    @Value("${cloud.api.key}")
//    private String cloudeApiKey;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private WebClient webClient;
    private RequestRepo requestRepo;

    @Autowired
    public OpenAiServices(RequestRepo requestRepo) {
        this.requestRepo = requestRepo;
        this.webClient = WebClient.builder()
//                .baseUrl("https://api.openai.com/v1/chat/completions")
//                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
//                .defaultHeader("Content-Type", "application/json")
//                .baseUrl("https://api.anthropic.com/v1/messages")
//                .defaultHeader("x-api-key", cloudeApiKey)
//                .defaultHeader("anthropic-version", "2023-06-01")
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyAW2RJ9jreYV4CThrS57nERwHz8ZhPAHdM")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Requests processRequest(Requests request) {
        try {
            // Update status to PROCESSING
            request.setStatus(Requests.Status.Processing);
            request.setUpdatedAt(Instant.now());
            requestRepo.save(request);

            // Construct OpenAI API request
            String concatenatedText = "Instruction start here (Given an unstructured sentence, extract key information and represent it in a concise JSON format. Focus on identifying entities, actions, and relationships, and only include relevant fields in the output. Omit any null or default values. Prioritize accuracy and clarity in the extracted data.)Instruction End Here: Unstructure sentence start here ( " + request.getUnStructuredData()+")Unstructure sentence end here";
            Map<String, Object> requestBody = Map.of(
////                    "model", "claude-3-7-sonnet-20250219",  // Use GPT-4 or GPT-3.5-turbo
////                    "max_tokens", "500",
//                    "messages", new Object[]{
//                            Map.of("role", "system", "content", "You are a JSON formatting assistant."),
//                            Map.of("role", "user", "content", request.getUnStructuredData())
//                    }
                    "contents", new Object[] {
                            Map.of(
                                    "parts", new Object[] {
                                            Map.of("text", concatenatedText)
                                    }
                            )
                    }
            );

            // Call OpenAI API and wait for response
            Map<String, Object> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();  // Waits for response
            // Log the response to see its structure
            System.out.println("Response from Gemini API: " + response);

            if (response != null) {
                // Get the "candidates" list
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    // Extract the content from the first candidate
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

                    if (content != null) {
                        // Extract the parts array from content
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                        if (parts != null && !parts.isEmpty()) {
                            // Get the text from the first part
                            String structuredData = (String) parts.get(0).get("text");

                            // Update request with structured data
                            request.setStructuredData(structuredData);
                            request.setStatus(Requests.Status.Completed);
                        } else {
                            request.setStatus(Requests.Status.Failed);
                            request.setErrorMessage("No parts found in content.");
                        }
                    } else {
                        request.setStatus(Requests.Status.Failed);
                        request.setErrorMessage("No content found in the first candidate.");
                    }
                } else {
                    request.setStatus(Requests.Status.Failed);
                    request.setErrorMessage("No candidates found in the response.");
                }
            } else {
                request.setStatus(Requests.Status.Failed);
                request.setErrorMessage("Invalid response from Gemini.");
            }

        } catch (WebClientException e) {
            request.setStatus(Requests.Status.Failed);
            request.setErrorMessage("Gemini API Error: " + e.getMessage());
        } catch (Exception e) {
            request.setStatus(Requests.Status.Failed);
            request.setErrorMessage("Unexpected Error: " + e.getMessage());
        }

        // Update the request in DB
        request.setUpdatedAt(Instant.now());
        requestRepo.save(request);

        return request;
    }

}
