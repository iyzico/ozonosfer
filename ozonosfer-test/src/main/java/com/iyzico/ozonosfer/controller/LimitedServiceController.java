package com.iyzico.ozonosfer.controller;

import com.iyzico.ozonosfer.domain.request.SampleRequest;
import com.iyzico.ozonosfer.domain.model.Message;
import com.iyzico.ozonosfer.domain.service.LimitedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitedServiceController {

    @Autowired
    LimitedService limitedService;

    @PostMapping("/limitedService")
    public Message greeting(@RequestBody SampleRequest sampleRequest) {
        try {
            Message responseMessage = limitedService.getMessage(sampleRequest);
            return responseMessage;
        }
        catch (Exception e){
            return new Message("Rate limit exceeded");
        }

    }
}