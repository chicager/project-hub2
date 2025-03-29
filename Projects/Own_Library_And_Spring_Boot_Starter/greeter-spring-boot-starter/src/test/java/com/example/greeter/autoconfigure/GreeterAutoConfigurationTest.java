package com.example.greeter.autoconfigure;

import com.example.greeter.Greeter;
import com.example.greeter.format.GreetingFormat;
import com.example.greeter.formatter.GreetingFormatter;
import com.example.greeter.formatter.HtmlFormatter;
import com.example.greeter.formatter.JsonFormatter;
import com.example.greeter.formatter.TextFormatter;
import com.example.greeter.template.GreetingTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GreeterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class))
            .withPropertyValues(
                    "greeter.formatting.datetime-pattern=yyyy-MM-dd HH:mm:ss",
                    "greeter.formatting.html.wrapper-class=default-wrapper",
                    "greeter.formatting.html.message-class=default-message",
                    "greeter.formatting.html.metadata-class=default-metadata"
            );

    @Test
    void whenNoPropertiesSet_shouldCreateDefaultBeans() {
        contextRunner.run(context -> {
            // Verify all required beans are created
            assertThat(context).hasSingleBean(Greeter.class);
            assertThat(context).hasSingleBean(GreetingTemplate.class);
            assertThat(context).hasSingleBean(TextFormatter.class);
            assertThat(context).hasSingleBean(HtmlFormatter.class);
            assertThat(context).hasSingleBean(JsonFormatter.class);
        });
    }

    @Test
    void whenPropertiesSet_shouldConfigureBeansAccordingly() {
        contextRunner
                .withPropertyValues(
                        "greeter.prefix=Hi",
                        "greeter.suffix=!",
                        "greeter.formatting.datetime-pattern=yyyy-MM-dd",
                        "greeter.formatting.html.wrapper-class=wrapper",
                        "greeter.formatting.html.message-class=message",
                        "greeter.formatting.html.metadata-class=metadata",
                        "greeter.formatting.json.include-metadata=true",
                        "greeter.formatting.json.pretty-print=true"
                )
                .run(context -> {
                    // Verify Greeter is configured
                    assertThat(context).hasSingleBean(Greeter.class);

                    // Verify formatters are configured with properties
                    TextFormatter textFormatter = context.getBean(TextFormatter.class);
                    // Проверяем работу форматтера на тестовых данных
                    LocalDateTime testDate = LocalDateTime.of(2024, 3, 28, 12, 0);
                    String formattedDate = textFormatter.format(
                            new com.example.greeter.model.GreetingResponse("test", testDate, Locale.ENGLISH)
                    );
                    assertThat(formattedDate).contains("2024-03-28"); // проверяем что дата форматируется правильно

                    HtmlFormatter htmlFormatter = context.getBean(HtmlFormatter.class);
                    assertThat(htmlFormatter)
                            .hasFieldOrPropertyWithValue("wrapperClass", "wrapper")
                            .hasFieldOrPropertyWithValue("messageClass", "message")
                            .hasFieldOrPropertyWithValue("metadataClass", "metadata");

                    JsonFormatter jsonFormatter = context.getBean(JsonFormatter.class);
                    assertThat(jsonFormatter)
                            .hasFieldOrPropertyWithValue("includeMetadata", true)
                            .hasFieldOrPropertyWithValue("prettyPrint", true);

                    // Verify all formatters are collected
                    Greeter greeter = context.getBean(Greeter.class);
                    @SuppressWarnings("unchecked")
                    Map<GreetingFormat, GreetingFormatter> formatters = (Map<GreetingFormat, GreetingFormatter>)
                            org.springframework.test.util.ReflectionTestUtils.getField(greeter, "formatters");

                    assertThat(formatters)
                            .hasSize(3)
                            .containsKeys(GreetingFormat.TEXT, GreetingFormat.HTML, GreetingFormat.JSON)
                            .containsValues(textFormatter, htmlFormatter, jsonFormatter);
                });
    }
}
