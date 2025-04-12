package org.example.cache_redis_spring_boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cache_redis_spring_boot.model.Product;
import org.example.cache_redis_spring_boot.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
Testcontainers - это библиотека для Java, которая позволяет запускать Docker контейнеры во время выполнения тестов.
Что такое Testcontainers:
  Это инструмент, который позволяет запускать Docker контейнеры как часть ваших тестов
  Он автоматически управляет жизненным циклом контейнеров (запуск, остановка, очистка)
  Позволяет тестировать приложения с реальными зависимостями (базы данных, кэши, очереди и т.д.)
Преимущества использования Testcontainers:
  Тесты работают с реальным Redis, а не с эмуляцией
  Изоляция тестов - каждый тест запускает свой собственный контейнер
  Автоматическая очистка - контейнеры удаляются после тестов
  Воспроизводимость - тесты будут работать одинаково на любой машине с Docker
Без Testcontainers пришлось бы:
  Либо запускать Redis вручную перед тестами
  Либо использовать менее реалистичные тесты с моками
  Либо писать сложную логику для управления жизненным циклом Redis

@Testcontainers - это ключевая аннотация, которая активирует функционал Testcontainers в нашем тестовом классе.
Основная функция:
  Аннотация @Testcontainers говорит JUnit, что этот класс использует Testcontainers
  Она автоматически управляет жизненным циклом всех контейнеров, помеченных аннотацией @Container
Что происходит под капотом:
  Перед запуском тестов:
    Запускаются все контейнеры, помеченные @Container
    Настраиваются все необходимые свойства (в нашем случае - host и port для Redis)
  После завершения тестов:
    Останавливаются все контейнеры
    Удаляются все ресурсы
Без этой аннотации:
  Контейнеры не будут автоматически запускаться/останавливаться
  Придется вручную управлять жизненным циклом контейнеров
  Нужно будет писать код для запуска и остановки Redis в методах @BeforeAll и @AfterAll
Преимущества использования:
  Автоматизация - не нужно писать код для управления контейнерами
  Надежность - контейнеры гарантированно запустятся перед тестами и остановятся после
  Чистота кода - логика управления контейнерами вынесена из тестового класса
*/
@Testcontainers
/*
@SpringBootTest - Загружает весь контекст приложения: Когда запускается тест с этой аннотацией, Spring Boot загружает
все компоненты твоего приложения (например, сервисы, репозитории, конфигурации) так, как если бы запускалось
само приложение. Это значит, что все бины (компоненты) будут доступны в тесте, и можно будет их использовать.
*/
@SpringBootTest
/*
@AutoConfigureMockMvc автоматически настраивает MockMvc для тестирования веб-слоя приложения.
Давайте разберем:
  Что такое MockMvc?
    Это основной инструмент Spring для тестирования контроллеров
    Позволяет отправлять HTTP-запросы и проверять ответы без запуска реального сервера
    Имитирует HTTP-запросы к вашему приложению
  Что делает @AutoConfigureMockMvc:
    Автоматически создает и настраивает экземпляр MockMvc
    Настраивает все необходимые фильтры и конвертеры
    Интегрирует MockMvc с Spring Security (если она используется)
Без этой аннотации пришлось бы вручную настраивать MockMvc
Вместо этой аннотации можно было бы использовать @WebMvcTest, но в нашем случае она не подходит потому что:
  У нас есть интеграция с Redis
  Мы используем реальный Redis контейнер через Testcontainers
  Нам нужно тестировать кэширование, которое работает на уровне сервисов
Когда использовать @WebMvcTest:
  Когда нужно тестировать только логику контроллера
  Когда не важна интеграция с внешними сервисами
  Когда нужно быстрое выполнение тестов
Когда использовать текущий подход (@SpringBootTest):
  Когда нужно тестировать полную интеграцию
  Когда важно проверить работу кэширования
  Когда нужно убедиться, что все компоненты работают вместе
*/
@AutoConfigureMockMvc
/*
@ActiveProfiles("test") активирует профиль "test" для тестового контекста Spring
Что такое профили в Spring?
  Профили - это способ настройки приложения для разных окружений
  Позволяют иметь разные конфигурации для разработки, тестирования, продакшена
  Настройки хранятся в файлах application-{profile}.properties или application-{profile}.yml
Что делает @ActiveProfiles("test"):
  Говорит Spring использовать настройки из application-test.properties
  Отключает настройки из application.properties
  Позволяет иметь специфичные настройки для тестов
*/
@ActiveProfiles("test")
class ProductControllerTest {

    /*
    @Container - это аннотация из библиотеки TestContainers, которая указывает,
    что данное поле представляет собой контейнер Docker, который должен быть запущен во время выполнения тестов.
    GenericContainer<?> - это класс TestContainers, который позволяет создавать и управлять Docker-контейнерами в тестах.
    Символ ? означает, что мы используем generic тип без конкретной спецификации.
    DockerImageName.parse("redis:latest") - указывает,
    что мы хотим использовать последнюю версию официального образа Redis из Docker Hub.
    .withExposedPorts(6379) - указывает, что контейнер должен открыть
    порт 6379 (стандартный порт Redis) для доступа к нему из тестов.
    Практическое значение этого кода:
      Когда запускаются тесты, TestContainers автоматически:
        Скачает образ Redis из Docker Hub (если его еще нет локально)
        Создаст и запустит контейнер с Redis
        Пробросит порт 6379 для доступа к Redis
        После завершения тестов автоматически остановит и удалит контейнер
      Это позволяет:
        Тестировать код с реальным Redis, а не с моками
        Иметь чистое состояние Redis для каждого запуска тестов
        Не зависеть от локально установленного Redis
        Гарантировать изоляцию тестового окружения
    */
    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    /*
    @DynamicPropertySource - это аннотация Spring Framework, которая позволяет динамически добавлять
    свойства в контекст Spring во время выполнения тестов. Это особенно полезно при работе с контейнерами,
    так как их параметры (хост, порт) становятся известны только после запуска.
    DynamicPropertyRegistry registry - реестр, через который можно добавлять динамические свойства в конфигурацию Spring.
    registry.add("spring.data.redis.host", redis::getHost):
      spring.data.redis.host - это свойство Spring, которое определяет хост для подключения к Redis
      redis::getHost - метод, который возвращает IP-адрес или hostname контейнера Redis
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379)):
      spring.data.redis.port - свойство Spring для порта Redis
      redis.getMappedPort(6379) - возвращает реальный порт на хост-машине, который соответствует порту 6379 внутри контейнера
    */
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    /*
    Мы используем MockMvc, который не требует реального HTTP-сервера
    Тесты выполняются в изолированном окружении
    Настройка порта не влияет на работу тестов
    */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        productService.updateProduct(testProduct);
    }

    @Test
    void testGetProduct() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void testUpdateProduct() throws Exception {
        testProduct.setName("Updated Name");

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        // Проверяем, что продукт действительно удален
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Добавляем еще продукты
        for (int i = 2; i <= 5; i++) {
            Product product = Product.builder()
                    .id((long) i)
                    .name("Product " + i)
                    .price(new BigDecimal("100.00"))
                    .build();
            productService.updateProduct(product);
        }

        mockMvc.perform(get("/api/products/list?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testClearCache() throws Exception {
        mockMvc.perform(delete("/api/products/cache"))
                .andExpect(status().isOk());
    }

    @Test
    void testInitTestData() throws Exception {
        mockMvc.perform(post("/api/products/init"))
                .andExpect(status().isOk());

        // Проверяем, что тестовые данные созданы
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }
}