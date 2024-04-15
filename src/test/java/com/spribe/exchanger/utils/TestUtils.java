package com.spribe.exchanger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    public static final String TEST_CURRENCY_CODE = "USD";
    public static final String TEST_CURRENCY_NAME = "United States Dollar";

    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    public static <T> T getMockData(String fileName, TypeReference<T> typeReference) {
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream("mockdata/" + fileName + ".json");
            if (inputStream == null) {
                throw new IOException("File not found: " + fileName);
            }
            return mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            log.error("Error reading mock data from file: {}", e.getMessage());
            return null;
        }
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
