package com.modelguard.service;
import com.modelguard.configs.CustomPromptConfig;
import com.modelguard.model.EmailClassificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailClassifierService {
    // Service class to classify user email categories
    private static final Logger log = LoggerFactory.getLogger(EmailClassifierService.class);

    private final ChatClient chatClient;
    private final CustomPromptConfig customPromptConfig;

    public EmailClassifierService(ChatClient.Builder chatClientBuilder, CustomPromptConfig customPromptConfig) {
        this.customPromptConfig = customPromptConfig;

        String systemPrompt = Objects.requireNonNull(
                customPromptConfig.getSystemPrompt(),
                "systemPrompt must not be null! Check spring.config.import and prompts/v1.0.yml structure."
        );
        // Dynamically inject system prompt from YAML
      this.chatClient = chatClientBuilder
                        .defaultSystem(customPromptConfig.getSystemPrompt()).build();
    }

    // Method to classify email type
    public EmailClassificationResult classify(String emailBody){
        PromptTemplate promptTemplate = new PromptTemplate(customPromptConfig.getUserPromptTemplate());
        var message = promptTemplate.createMessage(Map.of("emailBody", emailBody));
        try{
           return chatClient.prompt()
                    .messages(message)
                    .call()
                    .entity(EmailClassificationResult.class);
        } catch (Exception e) {
            log.error("Error executing EmailClassifierService (Version {}): {}",
                    customPromptConfig.getVersion(), e.getMessage(), e);
            throw new RuntimeException("Email classification failed", e);
        }
    }

    public String getActivePromptVersion() {
        return customPromptConfig.getVersion();
    }
}
