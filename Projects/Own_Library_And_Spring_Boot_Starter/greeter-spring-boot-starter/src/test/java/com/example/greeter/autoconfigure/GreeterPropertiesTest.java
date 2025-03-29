package com.example.greeter.autoconfigure;

import com.example.greeter.format.GreetingFormat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

/*
Аннотация @EnableConfigurationProperties(GreeterProperties.class) нужна для того, чтобы включить
механизм привязки свойств Spring Boot к нашему классу GreeterProperties.
Давайте разберем подробнее:
  GreeterProperties - это класс с аннотацией @ConfigurationProperties(prefix = "greeter"), который отвечает за:
    Определение структуры конфигурационных свойств
    Привязку значений из файлов свойств/YAML к полям класса
    Предоставление типобезопасного доступа к конфигурации
  В реальном приложении эта привязка включается автоматически через:
    @EnableConfigurationProperties в классе GreeterAutoConfiguration
    Или через сканирование компонентов с @ConfigurationPropertiesScan
  Но в тестах нам нужно явно включить эту функциональность, поэтому мы:
    Добавляем @EnableConfigurationProperties(GreeterProperties.class) на тестовый класс
    Это говорит Spring: "создай бин типа GreeterProperties и настрой его из свойств"
    Без этой аннотации Spring не будет знать, что нужно создать и настроить этот бин
  Если бы мы убрали эту аннотацию:
    Spring не создал бы бин GreeterProperties
    Попытка получить бин через context.getBean(GreeterProperties.class) привела бы к ошибке
    Мы не смогли бы протестировать привязку свойств
*/
@EnableConfigurationProperties(GreeterProperties.class)
class GreeterPropertiesTest {

    /*
    ApplicationContextRunner - это специальный тестовый инструмент Spring Boot,
    который позволяет нам создать временный контекст приложения для тестирования.
    Это как будто мы запускаем мини-версию нашего приложения, но только для теста.
    Здесь происходит следующее:
      Мы создаем "песочницу" для тестирования (ApplicationContextRunner)
      Говорим этой "песочнице" использовать настройки из нашего тестового
        класса (.withUserConfiguration(GreeterPropertiesTest.class))
    Это нужно потому что:
      В реальном приложении Spring создает контекст автоматически при запуске
      В тестах нам нужно создать такой же контекст, но временный и изолированный
      Мы хотим иметь возможность менять настройки для каждого теста отдельно
    Это как если бы для каждого теста мы:
      Создавали отдельное мини-приложение
      Настраивали его как нам нужно
      Проверяли что всё работает правильно
      И потом удаляли это мини-приложение
    Такой подход позволяет изолировать тесты друг от друга и тестировать разные сценарии настройки нашего стартера.
    */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GreeterPropertiesTest.class);

    @Test
    void whenNoPropertiesSet_shouldUseDefaults() {
        // Запускаем тест без настроек
        /*
        contextRunner - это не сам контекст, а фабрика для создания контекстов
        Каждый вызов .run() создает новый, изолированный контекст
        После выполнения блока в .run() контекст автоматически закрывается
        */
        contextRunner.run(context -> {
            GreeterProperties properties = context.getBean(GreeterProperties.class);

            // Verify default values
            assertThat(properties.getPrefix()).isNull();
            assertThat(properties.getSuffix()).isNull();
            assertThat(properties.getDefaultFormat()).isNull();

            // Verify default formatting properties
            assertThat(properties.getFormatting().getDatetimePattern()).isNull();

            // Verify default HTML properties
            assertThat(properties.getFormatting().getHtml().getWrapperClass()).isNull();
            assertThat(properties.getFormatting().getHtml().getMessageClass()).isNull();
            assertThat(properties.getFormatting().getHtml().getMetadataClass()).isNull();

            // Verify default JSON properties
            assertThat(properties.getFormatting().getJson().isIncludeMetadata()).isFalse();
            assertThat(properties.getFormatting().getJson().isPrettyPrint()).isFalse();
        });
    }

    @Test
    void whenPropertiesSet_shouldBindCorrectly() {
        // Запускаем тест с нашими настройками
        /*
        contextRunner - это не сам контекст, а фабрика для создания контекстов
        Каждый вызов .run() создает новый, изолированный контекст
        После выполнения блока в .run() контекст автоматически закрывается
        */
        contextRunner
                .withPropertyValues(
                        "greeter.prefix=Hello",
                        "greeter.suffix=!",
                        "greeter.default-format=HTML",
                        "greeter.formatting.datetime-pattern=yyyy-MM-dd HH:mm:ss",
                        "greeter.formatting.html.wrapper-class=greeting-wrapper",
                        "greeter.formatting.html.message-class=greeting-message",
                        "greeter.formatting.html.metadata-class=greeting-metadata",
                        "greeter.formatting.json.include-metadata=true",
                        "greeter.formatting.json.pretty-print=true"
                )
                .run(context -> {
                    GreeterProperties properties = context.getBean(GreeterProperties.class);

                    // Verify main properties
                    assertThat(properties.getPrefix()).isEqualTo("Hello");
                    assertThat(properties.getSuffix()).isEqualTo("!");
                    assertThat(properties.getDefaultFormat()).isEqualTo(GreetingFormat.HTML);

                    // Verify formatting properties
                    assertThat(properties.getFormatting().getDatetimePattern())
                            .isEqualTo("yyyy-MM-dd HH:mm:ss");

                    // Verify HTML properties
                    assertThat(properties.getFormatting().getHtml().getWrapperClass())
                            .isEqualTo("greeting-wrapper");
                    assertThat(properties.getFormatting().getHtml().getMessageClass())
                            .isEqualTo("greeting-message");
                    assertThat(properties.getFormatting().getHtml().getMetadataClass())
                            .isEqualTo("greeting-metadata");

                    // Verify JSON properties
                    assertThat(properties.getFormatting().getJson().isIncludeMetadata()).isTrue();
                    assertThat(properties.getFormatting().getJson().isPrettyPrint()).isTrue();
                });
    }
}