package com.modelguard.controllers;

import com.modelguard.model.EmailClassificationResult;
import com.modelguard.service.EmailClassifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/v1/eval-test")
public class PostManTestsController {

    @Autowired
    private EmailClassifierService emailClassifierService;

    @PostMapping("/classify-test")
    public ResponseEntity<EmailClassificationResult> testClassifier(@RequestBody Map<String, String> payload) {
        String emailText = payload.getOrDefault("text", "I need a refund for duplicate charge #1234.");
        EmailClassificationResult result = emailClassifierService.classify(emailText);
        return ResponseEntity.ok(result);
    }
}
