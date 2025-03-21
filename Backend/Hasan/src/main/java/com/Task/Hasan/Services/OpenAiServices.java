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
            // Update status to PROCESSING
            request.setStatus(Requests.Status.Processing);
            request.setUpdatedAt(Instant.now());
            requestRepo.save(request);
            // Construct OpenAI API 


            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String concatenatedText = "Instruction start here: Given an unstructured sentence, generate a concise JSON response containing exactly two fields: 'request' and 'answer'. The 'request' field must exactly match the user's original input without any modifications or additional formatting like parentheses, quotes, or brackets. The 'answer' field must explicitly resolve any relative expressions (such as dates or times) based strictly on today's date (" + today + "). Do not include explanations or additional text. Omit any null or default values. Instruction end here. Unstructured sentence start here: (" + request.getUnStructuredData() + ") Unstructured sentence end here.";
            Map<String, Object> requestBody = Map.of(
////                    "model", "claude-3-7-sonnet-20250219",  //  GPT-3.5-turbo
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
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String structuredData = (String) parts.get(0).get("text");
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
