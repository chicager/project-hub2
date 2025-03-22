package org.example.work_with_properties_spring_boot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.work_with_properties_spring_boot.service.ConfigurationDemoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/*
Аннотация @Profile используется для условной регистрации бинов в зависимости от активных профилей.
В Spring Boot, если не указан явно никакой профиль, по умолчанию активируется профиль default.
*/

@Slf4j
@Component
@Profile("!test") // Этот бин не будет создан, если активен профиль "test"
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {
    private final ConfigurationDemoService configurationDemoService;

    /*
    Метод run() в классе StartupRunner выполнится автоматически после старта приложения.
    Это происходит благодаря нескольким факторам:
     -Класс StartupRunner реализует интерфейс CommandLineRunner. Это специальный
      интерфейс Spring Boot, который используется для выполнения кода после того,
      как Spring контейнер полностью инициализирован.
     -Класс помечен аннотацией @Component, что означает, что Spring создаст его бин
      и добавит в контекст приложения.
     -Spring Boot автоматически находит все бины, реализующие интерфейс CommandLineRunner
      (или ApplicationRunner), и вызывает их метод run() после того, как приложение
      полностью загрузилось.
    Процесс работает следующим образом:
      Spring Boot запускает приложение
      Создаются все бины
      Настраивается контекст приложения
      После полной инициализации контекста, Spring Boot ищет все бины типа CommandLineRunner
      Для каждого найденного CommandLineRunner вызывается метод run()
    Важно отметить, что CommandLineRunner - это один из способов выполнить код при
    старте приложения. Другие способы включают:
      Использование @PostConstruct в компонентах
      Реализацию интерфейса ApplicationRunner (похож на CommandLineRunner, но получает
      аргументы в другом формате)
      Использование слушателей событий Spring (ApplicationListener)
    CommandLineRunner часто используется для:
      Инициализации данных при старте
      Проверки конфигурации
      Выполнения задач по настройке приложения
      Демонстрационных целей, как в вашем случае
    */
    /*
    Разница между CommandLineRunner и ApplicationRunner заключается в формате аргументов,
    которые они получают в методе run(). Давайте рассмотрим подробнее:
      CommandLineRunner:
        @FunctionalInterface
        public interface CommandLineRunner {
            void run(String... args) throws Exception;
        }

        Получает аргументы как массив строк (String... args)
        Аргументы передаются в "сыром" виде
        Удобен, когда нужен простой доступ к аргументам командной строки
      Пример использования:
        @Component
        public class MyCommandLineRunner implements CommandLineRunner {
            @Override
            public void run(String... args) {
                // args содержит сырые аргументы командной строки
                for (String arg : args) {
                    System.out.println(arg);
                }
            }
        }
      ApplicationRunner:
        @FunctionalInterface
        public interface ApplicationRunner {
            void run(ApplicationArguments args) throws Exception;
        }

        Получает аргументы в виде объекта ApplicationArguments
        Предоставляет более удобный API для работы с аргументами
        Поддерживает парсинг опций и неопционных аргументов
        Имеет методы для проверки наличия определенных опций
      Пример использования:
        @Component
        public class MyApplicationRunner implements ApplicationRunner {
            @Override
            public void run(ApplicationArguments args) {
                // Получить опции (аргументы, начинающиеся с --)
                Set<String> optionNames = args.getOptionNames();

                // Проверить наличие конкретной опции
                boolean hasDebug = args.containsOption("debug");

                // Получить значения для конкретной опции
                List<String> fileValues = args.getOptionValues("file");

                // Получить неопционные аргументы
                List<String> nonOptionArgs = args.getNonOptionArgs();
            }
        }

      Пример разницы в обработке аргументов:
        java -jar app.jar --debug file1.txt --file=data.csv other

        С CommandLineRunner:
          public void run(String... args) {
            // args будет содержать:
            // ["--debug", "file1.txt", "--file=data.csv", "other"]
          }

        С ApplicationRunner:
          public void run(ApplicationArguments args) {
            args.containsOption("debug");     // true
            args.getOptionValues("file");     // ["data.csv"]
            args.getNonOptionArgs();          // ["file1.txt", "other"]
        }

    Когда что использовать:
      Используйте CommandLineRunner, когда:
        Нужна простая обработка аргументов
        Хотите работать с сырыми аргументами командной строки
        Не требуется сложная логика разбора аргументов
      Используйте ApplicationRunner, когда:
        Нужно работать с именованными опциями (--option=value)
        Требуется более структурированный доступ к аргументам
        Нужно различать опции и неопционные аргументы
        Требуется проверка наличия определенных опций
    Оба интерфейса могут использоваться одновременно в одном приложении, и Spring Boot
    будет выполнять их в определенном порядке (который можно контролировать с помощью аннотации @Order).
        @Component
        @Order(1)  // Будет выполнен первым
        public class FirstRunner implements CommandLineRunner {
            @Override
            public void run(String... args) {
                // выполнится первым
            }
        }

        @Component
        @Order(2)  // Будет выполнен вторым
        public class SecondRunner implements ApplicationRunner {
            @Override
            public void run(ApplicationArguments args) {
                // выполнится вторым
            }
        }
    */
    @Override
    public void run(String... args) {
        log.info("Приложение запущено, демонстрация конфигурации:");
        configurationDemoService.demonstrateConfig();
    }
}
