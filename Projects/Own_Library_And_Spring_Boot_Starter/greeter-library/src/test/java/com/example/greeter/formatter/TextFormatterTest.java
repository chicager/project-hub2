package com.example.greeter.formatter;

import com.example.greeter.format.GreetingFormat;
import com.example.greeter.model.GreetingResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class TextFormatterTest {

    private final TextFormatter formatter = new TextFormatter("yyyy-MM-dd HH:mm:ss");

    @Test
    void format_ShouldReturnFormattedText() {
        // Arrange
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);

        // Act
        String result = formatter.format(response);

        // Assert
        String expectedText = String.format("%s [%s] (%s)",
                message,
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                locale.getDisplayLanguage()
        );

        assertEquals(expectedText, result, "Text format should match exactly");
    }

    @Test
    void getFormat_ShouldReturnTextFormat() {
        // Act
        GreetingFormat format = formatter.getFormat();

        // Assert
        assertEquals(GreetingFormat.TEXT, format);
    }

    @Test
    void format_WithRussianLocale_ShouldReturnFormattedText() {
        // Arrange
        String message = "Привет, мир!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("ru");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);

        // Act
        String result = formatter.format(response);

        // Assert
        String expectedText = String.format("%s [%s] (%s)",
                message,
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                locale.getDisplayLanguage(locale) // используем русскую локаль для отображения языка
        );

        assertEquals(expectedText, result, "Text format should match exactly for Russian locale");
    }

    @Test
    void format_WithDifferentDatePattern_ShouldUseCorrectPattern() {
        // Arrange
        String datePattern = "dd.MM.yyyy HH:mm";
        TextFormatter customFormatter = new TextFormatter(datePattern);
        String message = "Hello John!";
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 28, 12, 0);
        Locale locale = new Locale("en");
        GreetingResponse response = new GreetingResponse(message, timestamp, locale);

        // Act
        String result = customFormatter.format(response);

        // Assert
        String expectedText = String.format("%s [%s] (%s)",
                message,
                timestamp.format(DateTimeFormatter.ofPattern(datePattern)),
                locale.getDisplayLanguage()
        );

        assertEquals(expectedText, result, "Text format should use the specified date pattern");
    }

}