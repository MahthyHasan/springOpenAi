package com.Task.Hasan.Services;


import com.Task.Hasan.Entity.Requests;
import com.Task.Hasan.Repository.RequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RequestServices {

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private  OpenAiServices openAiServices;

    public void saveOrUpdate(Requests requests) throws Exception {
        if (requests.getId() == null) {
            requests.setStatus(Requests.Status.Pending);
            requests.setCreatedAt(Instant.now());
        }
        requests.setUpdatedAt(Instant.now());
        requestRepo.save(requests);

        openAiServices.processRequest(requests);
    }

    public List<Requests> getAllRequests() {
        return requestRepo.findAll();
    }

}
