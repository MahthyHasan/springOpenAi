package com.Task.Hasan.Controller;

import com.Task.Hasan.Entity.Requests;
import com.Task.Hasan.Services.OpenAiServices;
import com.Task.Hasan.Services.RequestServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/request")
public class RequestController {

    @Autowired
    private RequestServices requestServices;

    @Autowired
    private OpenAiServices openAiServices;

    @PostMapping(value = "/sent")
    public ResponseEntity<?> registerRequest(@RequestBody Requests request) {
        try {
            // Process the request using OpenAiServices
            Requests processedRequest = openAiServices.processRequest(request);

            // Check the status of the processed request and return the response accordingly
            if (processedRequest.getStatus() == Requests.Status.Completed) {
                // Return the structured data if the request is completed
                return ResponseEntity.ok(processedRequest.getStructuredData());
            } else {
                // Return error message if processing failed
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + processedRequest.getErrorMessage());
            }
        } catch (Exception e) {
            // Handle any unexpected errors during processing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected Error: " + e.getMessage());
        }
    }
}
