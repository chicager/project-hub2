package com.example.greeter;

import com.example.greeter.format.GreetingFormat;
import com.example.greeter.formatter.GreetingFormatter;
import com.example.greeter.formatter.HtmlFormatter;
import com.example.greeter.formatter.JsonFormatter;
import com.example.greeter.formatter.TextFormatter;
import com.example.greeter.template.GreetingTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreeterTest {

    @Mock
    private GreetingTemplate template;

    private Greeter greeter;
    private List<GreetingFormatter> formatters;

    @BeforeEach
    void setUp() {
        formatters = Arrays.asList(
                new TextFormatter("yyyy-MM-dd HH:mm:ss"),
                new HtmlFormatter("yyyy-MM-dd HH:mm:ss", "greeting", "message", "metadata"),
                new JsonFormatter("yyyy-MM-dd HH:mm:ss", true, true)
        );
        greeter = new Greeter(template, formatters);
    }

    @Test
    void greet_WithDefaultParameters_ShouldReturnFormattedMessage() {
        // Arrange
        String name = "John";
        String expectedMessage = "Hello John!";
        when(template.generateMessage(name, Locale.getDefault())).thenReturn(expectedMessage);

        // Act
        String result = greeter.greet(name);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(expectedMessage));
        verify(template).generateMessage(name, Locale.getDefault());
    }

    @Test
    void greet_WithCustomLocale_ShouldReturnLocalizedMessage() {
        // Arrange
        String name = "John";
        Locale locale = new Locale("ru");
        String expectedMessage = "Привет John!";
        when(template.generateMessage(name, locale)).thenReturn(expectedMessage);

        // Act
        String result = greeter.greet(name, locale, GreetingFormat.TEXT);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(expectedMessage));
        verify(template).generateMessage(name, locale);
    }

    @Test
    void greet_WithHtmlFormat_ShouldReturnHtmlFormattedMessage() {
        // Arrange
        String name = "John";
        String message = "Hello John!";
        when(template.generateMessage(name, Locale.getDefault())).thenReturn(message);

        // Act
        String result = greeter.greet(name, Locale.getDefault(), GreetingFormat.HTML);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<div class=\"greeting\">"));
        assertTrue(result.contains("<p class=\"message\">"));
        assertTrue(result.contains(message));
    }

    @Test
    void greet_WithJsonFormat_ShouldReturnJsonFormattedMessage() {
        // Arrange
        String name = "John";
        String message = "Hello John!";
        when(template.generateMessage(name, Locale.getDefault())).thenReturn(message);

        // Act
        String result = greeter.greet(name, Locale.getDefault(), GreetingFormat.JSON);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\"message\":"));
        assertTrue(result.contains("\"timestamp\":"));
        assertTrue(result.contains("\"locale\":"));
    }

    @Test
    void greet_WithUnsupportedFormat_ShouldThrowException() {
        // Arrange
        String name = "John";
        GreetingFormat unsupportedFormat = GreetingFormat.TEXT; // Используем существующий формат, но удаляем его из списка
        formatters = Arrays.asList(
                new HtmlFormatter("yyyy-MM-dd HH:mm:ss", "greeting", "message", "metadata"),
                new JsonFormatter("yyyy-MM-dd HH:mm:ss", true, true)
        );
        greeter = new Greeter(template, formatters);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                greeter.greet(name, Locale.getDefault(), unsupportedFormat)
        );
    }
}