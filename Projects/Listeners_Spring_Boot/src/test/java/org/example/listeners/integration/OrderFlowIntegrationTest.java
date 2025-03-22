package org.example.listeners.integration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.listeners.listener.OrderEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
@SpringBootTest - Указывает, что это интеграционный тест Spring Boot приложения.
Когда мы используем @SpringBootTest, Spring Boot запускает полноценное приложение для тестирования:
  Поднимается весь контекст Spring приложения
  Запускается встроенный веб-сервер (например, Tomcat) на случайном порту
  Поднимается база данных:
    Обычно для тестов используется встроенная in-memory база данных (например, H2)
    Или можно настроить тестовую базу данных отдельно через конфигурацию
Это отличается от модульных тестов (unit tests), где тестируется только один компонент в изоляции.
В случае с @SpringBootTest мы проводим полноценное интеграционное тестирование:

WebEnvironment.RANDOM_PORT - означает, что для теста будет запущен реальный веб-сервер на случайном свободном порту
*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest {

    /*
    Используется в интеграционных тестах Spring Boot, когда мы хотим протестировать
    наше приложение как настоящий веб-сервер.
    Представьте, что:
      Когда вы запускаете реальное приложение, оно работает на каком-то порту (например, 8080)
      Но когда мы тестируем, мы не можем использовать фиксированный порт, потому что:
        Этот порт может быть занят
        Если запускается несколько тестов одновременно, они будут конфликтовать
        На разных компьютерах могут быть разные настройки портов
    Поэтому Spring Boot в тестах:
      Запускает настоящий веб-сервер
      Выбирает случайный свободный порт (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
      И через аннотацию @LocalServerPort говорит нам: "Эй, я запустил сервер вот на этом порту"
    В нашем тесте мы используем этот порт для формирования URL:
      String url = String.format("http://localhost:%d/api/orders/test", port);
    Простая аналогия:
      Это как если бы вы арендовали помещение для магазина
      Вы не знаете заранее, какой будет адрес
      Арендодатель (Spring Boot) выбирает свободное помещение
      И сообщает вам адрес (@LocalServerPort)
      А вы уже используете этот адрес для доставки товаров (отправки запросов)
    Без @LocalServerPort нам пришлось бы:
      Либо использовать фиксированный порт (что плохо)
      Либо как-то по-другому узнавать, на каком порту запустился сервер
    */
    @LocalServerPort
    private int port;

    /*
    TestRestTemplate - это специальный инструмент Spring Boot для тестирования HTTP запросов,
    как будто мы обращаемся к нашему приложению "снаружи", как реальный клиент.
    Простая аналогия:
      Представьте, что ваше приложение - это магазин
      TestRestTemplate - это тестировщик, который проверяет работу магазина как обычный покупатель
      Он заходит через главный вход (HTTP запросы)
      Проверяет, как работает весь магазин целиком
    В нашем тесте мы используем его так:
      ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
    Это как если бы мы:
      Открыли браузер
      Зашли по адресу нашего приложения
      Отправили POST запрос
      Получили ответ
    Почему не использовать обычный RestTemplate?
      TestRestTemplate специально создан для тестов
      Он умеет работать со случайными портами (которые мы получаем через @LocalServerPort)
      Он автоматически настраивается для работы с тестовым окружением
      Он более удобен для тестирования (например, лучше обрабатывает ошибки)
    Сравнение с другими подходами:
      MockMvc (который мы используем в OrderControllerTest):
        Тестирует контроллер напрямую, без запуска сервера
        Быстрее, но менее реалистично
      TestRestTemplate:
        Тестирует через реальные HTTP запросы
        Медленнее, но тестирует всё приложение целиком
        Ближе к тому, как приложение будет использоваться в реальности
    То есть:
      MockMvc - как тестировать кассира отдельно
      TestRestTemplate - как тестировать весь магазин, заходя как обычный покупатель
    */
    @Autowired
    private TestRestTemplate restTemplate;

    /*
    ListAppender<ILoggingEvent> - это специализированный класс из библиотеки Logback:
      Перехватывает все события логирования (logging events)
      Сохраняет их в коллекцию типа List<ILoggingEvent>
      Предоставляет программный доступ к этим событиям через listAppender.list
    Представьте, что:
      В нашем приложении есть система логирования (как записная книжка, куда записываются все важные события)
      Обычно эти логи записываются в файл или выводятся в консоль
      Но в тестах нам нужно проверить, что правильные сообщения были записаны в эту "записную книжку"
    ListAppender - это специальный инструмент, который:
      Работает как "временная записная книжка" для тестов
      Вместо записи в файл или консоль, сохраняет все логи в список в памяти
      Позволяет нам потом проверить эти записи
    Пример использования:
      // Создаём "временную записную книжку"
      private ListAppender<ILoggingEvent> listAppender;

      // Когда происходит какое-то действие в тесте
      orderEventListener.handleOrderCreatedEvent(orderCreatedEvent);

      // Смотрим, что записалось в нашу "книжку"
      List<ILoggingEvent> logsList = listAppender.list;

      // Проверяем, что нужная запись там есть
      assertTrue(logsList.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("Новый заказ создан!")));
    Простая аналогия:
      Это как если бы вы проверяли работу официанта
      Обычно официант записывает заказы в блокнот
      Но для проверки вы даете ему специальный тестовый блокнот
      И потом проверяете, правильно ли он записал заказы в этот блокнот
    Зачем это нужно:
      Проверить, что система правильно логирует события
      Проверить порядок логирования
      Проверить содержимое сообщений
      Убедиться, что все важные события фиксируются
    В нашем случае мы проверяем:
      Что создание заказа логируется
      Что завершение заказа логируется
      Что все сообщения содержат нужную информацию
      Что события логируются в правильном порядке
    */
    private ListAppender<ILoggingEvent> listAppender;

    /*
    Logger - это класс из библиотеки логирования (в нашем случае SLF4J/Logback),
    который отвечает за ведение логов в приложении.
    logger - локальная и используется только внутри метода setUp(). Это нормально, потому что:
      Нам не нужно хранить сам логгер, так как:
        Логгер в SLF4J/Logback это синглтон
        Когда мы вызываем LoggerFactory.getLogger(OrderEventListener.class), мы всегда получаем один и тот же экземпляр логгера
        После того как мы привязали к нему наш listAppender, связь сохраняется
      А вот listAppender мы храним как поле класса, потому что:
        Именно в него будут попадать все логи
        Нам нужен доступ к нему в каждом тестовом методе для проверки логов
    Как это работает:
      @Test
      void someTest() {
          // Когда OrderEventListener что-то логирует
          orderEventListener.doSomething();

          // Логи попадают в наш listAppender, хотя logger был локальной переменной
          List<ILoggingEvent> logs = listAppender.list;
          // Проверяем логи...
      }
    Это работает потому что:
      Логгер - это глобальный объект (синглтон)
      Когда мы добавляем к нему аппендер, эта связь сохраняется
      Все последующие логи будут попадать в этот аппендер
      Нам достаточно хранить ссылку только на аппендер
    */
    @BeforeEach // Этот метод будет выполняться перед каждым тестом
    void setUp() {
        // Получаем логгер для класса OrderEventListener
        Logger logger = (Logger) LoggerFactory.getLogger(OrderEventListener.class);
        // Создаем новый "перехватчик" логов
        listAppender = new ListAppender<>();
        // Запускаем перехватчик (без этого он не будет работать)
        listAppender.start();
        // Проверяем, есть ли уже прикрепленный перехватчик с таким именем
        if (logger.getAppender("testAppender") != null) {
            // Если есть - отсоединяем его, чтобы избежать дублирования
            logger.detachAppender("testAppender");
        }
        // Даем имя нашему перехватчику
        listAppender.setName("testAppender");
        // Прикрепляем перехватчик к логгеру
        logger.addAppender(listAppender);
        // Устанавливаем уровень логирования INFO
        logger.setLevel(Level.INFO);
        // Очищаем список логов от предыдущих тестов
        listAppender.list.clear();
    }

    @Test
    void testCompleteOrderFlow() {
        // when
        // %d будет заменен на порт, например если port = 8080, получим: "http://localhost:8080/api/orders/test"
        String url = String.format("http://localhost:%d/api/orders/test", port);
        /*
        Код для выполнения HTTP POST-запроса, где:
          restTemplate - это клиент Spring для выполнения HTTP-запросов
          postForEntity - метод для выполнения POST-запроса, который принимает три параметра:
            url - адрес, куда отправляем запрос
            null - тело запроса (в нашем случае пустое, поэтому null)
            String.class - тип данных, который ожидаем получить в ответе
          ResponseEntity<String> - объект, который содержит:
            Тело ответа (как String)
            HTTP статус (200 OK, 404 Not Found и т.д.)
            Заголовки ответа
        */
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("События заказа протестированы", response.getBody());

        // Проверяем, что все события были обработаны
        List<ILoggingEvent> logsList = listAppender.list;

        // Проверяем создание заказа
        assertTrue(logsList.stream()
                .anyMatch(event -> event.getMessage().contains("Новый заказ создан!")));

        // Проверяем завершение заказа
        assertTrue(logsList.stream()
                .anyMatch(event -> event.getMessage().contains("Заказ выполнен!")));

        // Проверяем общие события
        assertEquals(2, logsList.stream()
                .filter(event -> event.getMessage().contains("Получено событие заказа"))
                .count());

        // Проверяем порядок событий
        List<String> eventOrder = logsList.stream()
                .map(ILoggingEvent::getMessage)
                .filter(msg -> msg.contains("заказ"))
                .toList();

        assertTrue(eventOrder.get(0).contains("Новый заказ создан!"));
        assertTrue(eventOrder.get(1).contains("Получено событие заказа"));
        assertTrue(eventOrder.get(2).contains("Заказ выполнен!"));
        assertTrue(eventOrder.get(3).contains("Получено событие заказа"));
    }

    @Test
    void testOrderFlow_WithInvalidUrl() {
        // when
        String url = String.format("http://localhost:%d/api/orders/invalid", port);
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
