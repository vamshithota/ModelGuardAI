package com.modelguard.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "eval.prompt")
public class CustomPromptConfig {
    private String version;
    private String timestamp;
    private String description;
    private String systemPrompt;
    private String userPromptTemplate;
    private List<FewShotExample> fewShotExamples;

    public record FewShotExample(String input, String category, String summary) {}

    // Getters and Setters for Spring Boot Binding
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getUserPromptTemplate() { return userPromptTemplate; }
    public void setUserPromptTemplate(String userPromptTemplate) { this.userPromptTemplate = userPromptTemplate; }
    public void setFewShotExamples(List<FewShotExample> fewShotExamples) { this.fewShotExamples = fewShotExamples; }
}
