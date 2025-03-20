package com.Task.Hasan.Controller;

import com.Task.Hasan.Entity.Requests;
import com.Task.Hasan.Services.OpenAiServices;
import com.Task.Hasan.Services.RequestServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            Requests processedRequest = openAiServices.processRequest(request);
            if (processedRequest.getStatus() == Requests.Status.Completed) {
                return ResponseEntity.ok(Map.of("data", processedRequest.getStructuredData()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + processedRequest.getErrorMessage());            }
        } catch (Exception e) {
            // Handle any unexpected errors during processing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected Error: " + e.getMessage());
        }
    }
    @GetMapping("/all")
    public ResponseEntity<List<Requests>> getAllRequests() {
        List<Requests> requests = requestServices.getAllRequests();
        return ResponseEntity.ok(requests);
    }
}
