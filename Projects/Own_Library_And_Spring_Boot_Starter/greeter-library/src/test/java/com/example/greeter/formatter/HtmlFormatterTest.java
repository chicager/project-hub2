package com.example.greeter.formatter;

import com.example.greeter.format.GreetingFormat;
import com.example.greeter.model.GreetingResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class HtmlFormatterTest {

    private final HtmlFormatter formatter = new HtmlFormatter(
            "yyyy-MM-dd HH:mm:ss",
            "greeting",
            "message",
            "metadata"
    );

    @Test
    void format_ShouldReturnFormattedHtml() {
        // Arrange
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);

        // Act
        String result = formatter.format(response).replaceAll("\\s+", " ").trim();

        // Assert
        assertNotNull(result);
        // Проверяем основную структуру
        assertTrue(result.contains("<div class=\"greeting\">"), "Should contain wrapper div");
        assertTrue(result.contains("<p class=\"message\">" + message + "</p>"), "Should contain message paragraph");

        // Проверяем метаданные
        String timestampStr = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        assertTrue(result.contains("<small class=\"metadata\">" + timestampStr + "</small>"), "Should contain timestamp");
        assertTrue(result.contains("<small class=\"metadata\">" + locale.getDisplayLanguage() + "</small>"), "Should contain locale");
    }

    @Test
    void getFormat_ShouldReturnHtmlFormat() {
        // Act
        GreetingFormat format = formatter.getFormat();

        // Assert
        assertEquals(GreetingFormat.HTML, format);
    }
}