package com.Task.Hasan.Services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PromptTemplates {

    public static String getIntentClarificationPrompt(String userInput) {
        return "Given the sentence: '" + userInput +
                "', clearly identify and return a single concise question representing the user's intent. " +
                "Provide only the question without explanations.";
    }

    public static String getFinalStructuredPrompt(String userInput, String clarifiedQuestion) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "Instruction: Generate a concise JSON response containing exactly two fields: " +
                "'request' (exactly match user's input: '" + userInput + "') and " +
                "'response' (clearly and explicitly answer the clarified question: '" + clarifiedQuestion + "', but dont' return clarified question text inside the final response " +
                "field must explicitly resolve any relative expressions (such as dates or times) based strictly on today's date: " + today + "). " +
                "Do not include additional text.";
    }
}
