package com.Task.Hasan.Services;

import com.Task.Hasan.Entity.Requests;
import com.Task.Hasan.Repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyB3JMk4gm50b6J-IahD5SCoFsO-zK8ewxg")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Requests processRequest(Requests request) {
        try {
            request.setStatus(Requests.Status.Processing);
            request.setUpdatedAt(Instant.now());
            requestRepo.save(request);

            // Step 1: Intent Clarification
            String clarificationPrompt = PromptTemplates.getIntentClarificationPrompt(request.getUnStructuredData());
            Map<String, Object> clarificationBody = Map.of(
                    "contents", new Object[]{ Map.of("parts", new Object[]{Map.of("text", clarificationPrompt)}) }
            );

            Map<String, Object> clarificationResponse = webClient.post()
                    .bodyValue(clarificationBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String clarifiedQuestion = extractGeminiResponse(clarificationResponse);

            if (clarifiedQuestion == null || clarifiedQuestion.isEmpty()) {
                throw new RuntimeException("Clarification response is empty");
            }
            request.setClarifiedIntent(clarifiedQuestion);
            requestRepo.save(request);


            try {
                Thread.sleep(3000);  // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted during sleep: " + e.getMessage());
            }

            // Step 2: Structured Prompt Generation
            String finalPrompt = PromptTemplates.getFinalStructuredPrompt(request.getUnStructuredData(), clarifiedQuestion);
            Map<String, Object> finalRequestBody = Map.of(
                    "contents", new Object[]{ Map.of("parts", new Object[]{Map.of("text", finalPrompt)}) }
            );

            Map<String, Object> finalResponse = webClient.post()
                    .bodyValue(finalRequestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String structuredData = extractGeminiResponse(finalResponse);

            if (structuredData == null || structuredData.isEmpty()) {
                throw new RuntimeException("Final structured response is empty");
            }

            request.setStructuredData(structuredData);
            request.setStatus(Requests.Status.Completed);

        } catch (WebClientException e) {
            request.setStatus(Requests.Status.Failed);
            request.setErrorMessage("Gemini API Error: " + e.getMessage());
            System.err.println("Gemini API Exception: " + e.getMessage());
        } catch (Exception e) {
            request.setStatus(Requests.Status.Failed);
            request.setErrorMessage("Unexpected Error: " + e.getMessage());
            System.err.println("Unexpected Exception: " + e.getMessage());
        }

        request.setUpdatedAt(Instant.now());
        requestRepo.save(request);
        return request;
    }

    private String extractGeminiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String result = (String) parts.get(0).get("text");
                        System.out.println("Gemini response extracted: " + result);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting response: " + e.getMessage());
        }
        return null;
    }
}
