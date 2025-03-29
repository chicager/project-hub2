package org.example.greeterexample.controller;

import com.example.greeter.Greeter;
import com.example.greeter.format.GreetingFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/*
@WebMvcTest(GreetController.class):
  Загружает только веб-слой приложения (контроллеры, фильтры, перехватчики и т.д.)
  НЕ загружает полный контекст Spring приложения
  Автоматически настраивает MockMvc для тестирования REST endpoints
  Загружает только указанный контроллер (в нашем случае GreetController)
Почему это хорошо:
  @WebMvcTest(GreetController.class) // Загружаем только GreetController
  class GreetControllerTest {
    @Autowired
    private MockMvc mockMvc; // Автоматически настраивается

    @MockBean
    private TaskRepository taskRepository; // Нужно явно мокировать зависимости

    // Тесты...
  }
Сравнение с полным тестом @SpringBootTest:
  @SpringBootTest // Загружает ВСЁ приложение
  @AutoConfigureMockMvc // Нужно добавлять отдельно
  class GreetControllerTest {
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
В нашем случае @WebMvcTest(GreetControllerх.class) идеально подходит, потому что:
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
@WebMvcTest(GreetController.class)
class GreetControllerTest {

    /*
    MockMvc - это специальный класс Spring для тестирования веб-приложений, который позволяет имитировать HTTP-запросы
    @Autowired здесь используется потому что Spring сам создает реальный экземпляр MockMvc в тестовом контексте
    Этот объект создается автоматически благодаря аннотации @WebMvcTest
    Нам нужен реальный объект MockMvc, а не мок, потому что через него мы будем делать тестовые запросы
    */
    @Autowired
    private MockMvc mockMvc;

    /*
    Greeter - это наш сервис, который в реальном приложении делает бизнес-логику
    @MockBean создает мок (имитацию) этого сервиса и помещает его в контекст Spring вместо реального бина
    @MockBean используется в интеграционных тестат, @Mock в Unit тестах
    Мы используем мок потому что:
      Нам не нужна реальная логика сервиса при тестировании контроллера
      Мы хотим изолировать тестирование контроллера от других компонентов
      Можем контролировать поведение сервиса через when() и проверять вызовы через verify()
    */
    @MockBean
    private Greeter greeter;

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
    void greet_WithNoParameters_ShouldUseDefaults() throws Exception {
        // given
        when(greeter.greet(any(), any(), any())).thenReturn("Hello!");

        // when/then
        mockMvc.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello!"));

        verify(greeter).greet(null, Locale.getDefault(), GreetingFormat.TEXT);
    }

    @Test
    void greet_WithAllParameters_ShouldUseProvidedValues() throws Exception {
        // given
        when(greeter.greet(eq("John"), any(Locale.class), eq(GreetingFormat.HTML)))
                .thenReturn("<div>Hello, John!</div>");

        // when/then
        mockMvc.perform(get("/greet")
                        .param("name", "John")
                        .param("lang", "fr")
                        .param("format", "HTML"))
                .andExpect(status().isOk())
                .andExpect(content().string("<div>Hello, John!</div>"));

        verify(greeter).greet("John", new Locale("fr"), GreetingFormat.HTML);
    }

    @Test
    void greet_WithNameOnly_ShouldUseDefaultLocaleAndFormat() throws Exception {
        // given
        when(greeter.greet(eq("Alice"), any(), any())).thenReturn("Hello, Alice!");

        // when/then
        mockMvc.perform(get("/greet")
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Alice!"));

        verify(greeter).greet("Alice", Locale.getDefault(), GreetingFormat.TEXT);
    }

    @Test
    void greet_WithInvalidFormat_ShouldReturn400() throws Exception {
        // when/then
        mockMvc.perform(get("/greet")
                        .param("format", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void greet_WithInvalidLanguage_ShouldReturn400() throws Exception {
        // when/then
        mockMvc.perform(get("/greet")
                        .param("lang", "invalid"))
                .andExpect(status().isOk());

        verify(greeter).greet(null, new Locale("invalid"), GreetingFormat.TEXT);
    }
}