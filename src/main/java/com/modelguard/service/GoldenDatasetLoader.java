package com.modelguard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelguard.model.GoldenDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class GoldenDatasetLoader {

    private static final Logger log = LoggerFactory.getLogger(GoldenDatasetLoader.class);
    private final ObjectMapper objectMapper;

    public GoldenDatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public GoldenDataset loadDataset(String resourcePath) {
        log.info("Loading ground-truth Golden Dataset from classpath path: {}", resourcePath);
        try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
            GoldenDataset dataset = objectMapper.readValue(inputStream, GoldenDataset.class);
            log.info("Successfully loaded dataset version '{}' with {} test cases.",
                    dataset.datasetVersion(), dataset.testCases().size());
            return dataset;
        } catch (Exception e) {
            log.error("Failed to parse Golden Dataset from path: {}", resourcePath, e);
            throw new IllegalStateException("Could not load golden dataset file: " + resourcePath, e);
        }
    }
}
