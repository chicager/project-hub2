package com.example.greeter.template;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGreetingTemplateTest {

    private final SimpleGreetingTemplate template = new SimpleGreetingTemplate("Custom", "!");

    @Test
    void generateMessage_WithEnglishLocale_ShouldReturnEnglishGreeting() {
        // Arrange
        String name = "John";
        Locale locale = new Locale("en");

        // Act
        String result = template.generateMessage(name, locale);

        // Assert
        String expectedText = String.format("Hello %s !", name);
        assertEquals(expectedText, result, "English greeting should match the expected format");
    }

    @Test
    void generateMessage_WithRussianLocale_ShouldReturnRussianGreeting() {
        // Arrange
        String name = "Иван";
        Locale locale = new Locale("ru");

        // Act
        String result = template.generateMessage(name, locale);

        // Assert
        String expectedText = String.format("Привет %s !", name);
        assertEquals(expectedText, result, "Russian greeting should match the expected format");
    }

    @Test
    void generateMessage_WithCustomPrefixAndSuffix_ShouldUseCustomValues() {
        // Arrange
        String name = "John";
        Locale locale = new Locale("en");
        SimpleGreetingTemplate customTemplate = new SimpleGreetingTemplate("Hi", "!!!");

        // Act
        String result = customTemplate.generateMessage(name, locale);

        // Assert
        String expectedText = String.format("Hello %s !!!", name);
        assertEquals(expectedText, result, "Custom greeting should match the expected format");
    }
}