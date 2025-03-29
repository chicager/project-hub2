package com.example.greeter.formatter;

import com.example.greeter.format.GreetingFormat;
import com.example.greeter.model.GreetingResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class JsonFormatterTest {

    private final JsonFormatter formatter = new JsonFormatter(
            "yyyy-MM-dd HH:mm:ss",
            true,
            true
    );

    @Test
    void format_ShouldReturnFormattedJson() {
        // Arrange
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);

        // Act
        String result = formatter.format(response).replaceAll("\\s+", " ").trim();

        // Assert
        assertNotNull(result);

        // Проверяем структуру JSON с учетом форматирования
        String expectedJson = String.format(
                "{ \"message\": \"%s\", \"timestamp\": \"%s\", \"locale\": \"%s\" }",
                message,
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                locale.getDisplayLanguage()
        ).replaceAll("\\s+", " ").trim();

        assertEquals(expectedJson, result, "JSON structure should match exactly");
    }

    @Test
    void getFormat_ShouldReturnJsonFormat() {
        // Act
        GreetingFormat format = formatter.getFormat();

        // Assert
        assertEquals(GreetingFormat.JSON, format);
    }

    @Test
    void format_WithoutMetadata_ShouldReturnOnlyMessage() {
        // Arrange
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);
        JsonFormatter formatterWithoutMetadata = new JsonFormatter(
                "yyyy-MM-dd HH:mm:ss",
                false,  // без метаданных
                true    // с форматированием
        );

        // Act
        String result = formatterWithoutMetadata.format(response).replaceAll("\\s+", " ").trim();

        // Assert
        String expectedJson = String.format(
                "{ \"message\": \"%s\" }",
                message
        ).replaceAll("\\s+", " ").trim();

        assertEquals(expectedJson, result, "JSON should contain only message field");
    }

    @Test
    void format_WithoutPrettyPrint_ShouldReturnCompactJson() {
        // Arrange
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);
        JsonFormatter compactFormatter = new JsonFormatter(
                "yyyy-MM-dd HH:mm:ss",
                true,   // с метаданными
                false   // без форматирования
        );

        // Act
        String result = compactFormatter.format(response).trim();

        // Assert
        String expectedJson = String.format(
                "{\"message\":\"%s\",\"timestamp\":\"%s\",\"locale\":\"%s\"}",
                message,
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                locale.getDisplayLanguage()
        );

        assertEquals(expectedJson, result, "JSON should be compact without extra spaces");
    }
}