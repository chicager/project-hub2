package org.example.listeners.controller;

import org.example.listeners.model.Order;
import org.example.listeners.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
@WebMvcTest(OrderController.class):
  Загружает только веб-слой приложения (контроллеры, фильтры, перехватчики и т.д.)
  НЕ загружает полный контекст Spring приложения
  Автоматически настраивает MockMvc для тестирования REST endpoints
  Загружает только указанный контроллер (в нашем случае TaskController)
Почему это хорошо:
  @WebMvcTest(OrderController.class) // Загружаем только OrderController

Сравнение с полным тестом @SpringBootTest:
  @SpringBootTest // Загружает ВСЁ приложение
  @AutoConfigureMockMvc // Нужно добавлять отдельно
  class OrderControllerTest {
    // Тесты...
  }
Простая аналогия:
  @SpringBootTest - как запуск всего автомобиля для проверки только руля
  @WebMvcTest - как тестирование только руля на специальном стенде
Преимущества использования @WebMvcTest:
  Тесты запускаются БЫСТРЕЕ (загружается меньше компонентов)
  Тесты более ИЗОЛИРОВАННЫЕ (тестируем только веб-слой)
  Явно видно, какие зависимости нужны (через @MockBean)
  Меньше "шума" от других компонентов
В нашем случае @WebMvcTest(OrderController.class) идеально подходит, потому что:
  Мы тестируем только REST endpoints
  Нам нужен только один контроллер
  Мы можем мокировать репозиторий
  Нам не нужна реальная база данных или другие компоненты
Это делает наши тесты:
  Быстрыми
  Надежными
  Легко поддерживаемыми
  Сфокусированными на тестировании именно веб-слоя
*/
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    /*
    MockMvc позволяет симулировать http запросы и ответы (без запуска реального сервера)
    */
    @Autowired
    private MockMvc mockMvc;

    /*
    Если тест с @SpringBootTest или @WebMvcTest → используем @MockBean
    Если простой тест без Spring → используем @Mock
    */
    @MockBean
    private OrderService orderService;

    /*
    В обычном production-коде если метод объявляет throws Exception, то вызывающий код должен либо:
      Обработать исключение через try-catch
      Или пробросить его дальше через throws
    Но в случае с тестовыми методами ситуация особенная:
      Тестовые методы являются конечными точками выполнения
      Их выполняет фреймворк JUnit
      JUnit автоматически обрабатывает все исключения, которые возникают в тестовых методах
    То есть, когда мы пишем:
      @Test
      void testOrderEvents_ShouldReturnSuccessMessage() throws Exception {
        // test code
      }

    JUnit выступает в роли "обработчика" этих исключений. Внутри JUnit это выглядит примерно так:
      // Примерная внутренняя реализация JUnit
      try {
          runTestMethod();
      } catch (Exception e) {
          markTestAsFailed(e);
          reportError(e);
      }
    Поэтому:
      В обычном коде: throws Exception требует обработки выше
      В тестах: throws Exception обрабатывается фреймворком JUnit
    Можно переписать наш тест и без throws Exception, а с помощью try-catch, но такой подход:
      Делает код более громоздким
      Не дает никаких преимуществ, так как JUnit все равно обработает исключение
      Скрывает реальный стектрейс ошибки, что усложняет отладку
    Поэтому в тестах принято:
      ✅ Использовать throws Exception
      ❌ Не оборачивать код в try-catch
      ✅ Позволять JUnit обрабатывать исключения
    Это считается хорошей практикой в тестировании, хотя и может показаться противоречащим обычным
    правилам обработки исключений в Java.
    */
    @Test
    void testOrderEvents_ShouldReturnSuccessMessage() throws Exception {
        // when
        mockMvc.perform(post("/api/orders/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("События заказа протестированы"));

        // then
        verify(orderService, times(1)).createOrder(any(Order.class));
        verify(orderService, times(1)).completeOrder(any(Order.class));
    }
}