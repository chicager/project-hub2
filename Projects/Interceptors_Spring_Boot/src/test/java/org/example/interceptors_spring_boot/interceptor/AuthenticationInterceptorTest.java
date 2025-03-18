package org.example.interceptors_spring_boot.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
Аннотация @ExtendWith(MockitoExtension.class) нужна для интеграции Mockito с JUnit 5 (Jupiter). Давайте разберем подробнее:
  Что делает эта аннотация:
    Инициализирует моки, созданные с помощью аннотаций @Mock
    Обрабатывает аннотации @InjectMocks
    Проверяет правильность использования моков после каждого теста
    Очищает все моки после каждого теста
*/
@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    /*
    Аннотация @InjectMocks создает реальный экземпляр класса AuthenticationInterceptor и автоматически
    внедряет в него все моки, которые объявлены с аннотацией @Mock.
    Мы не можем использовать @Mock для AuthenticationInterceptor, потому что нам
    нужно тестировать реальную логику этого класса. Иначе:
      Все методы этого класса были бы "пустыми" (возвращали бы значения по умолчанию)
      Реальная логика аутентификации не выполнялась бы
      Нам пришлось бы вручную прописывать поведение каждого метода через when()
    Пример разницы:
      // С @InjectMocks - тестируем реальную логику
      when(request.getHeader("X-API-KEY")).thenReturn("invalid-key");
      boolean result = authenticationInterceptor.preHandle(request, response, null);
      assertFalse(result); // Реальная проверка API ключа выполняется

      // Если бы использовали @Mock - пришлось бы имитировать всю логику
      when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(false);
      // Это бессмысленно, так как мы не тестируем реальную логику

    Т. е. @InjectMocks используем для класса, который непосредственно тестируем
    */
    @InjectMocks
    private AuthenticationInterceptor authenticationInterceptor;

    /*
    Аннотация @Mock создает имитацию (мок) объекта HttpServletRequest.
    Объект не будет null (хотя так то будет) - Mockito создаст специальную заглушку, которая:
      По умолчанию:
        Для примитивов возвращает значения по умолчанию (0, false, etc.)
        Для объектов возвращает null
        Для коллекций возвращает пустые коллекции
    Важно понимать, что это не реальный HttpServletRequest, а его имитация:
      Нет реального HTTP запроса
      Нет подключения к серверу
      Все поведение нужно настраивать через when()
      Используется для изоляции тестируемого кода от реальной инфраструктуры
    */
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    void preHandle_WithValidApiKey_ShouldReturnTrue() throws Exception {
        // Given
        when(request.getHeader("X-API-KEY")).thenReturn("your-secret-key");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_WithInvalidApiKey_ShouldReturnFalse() throws Exception {
        // Given
        when(request.getHeader("X-API-KEY")).thenReturn("invalid-key");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void preHandle_WithMissingApiKey_ShouldReturnFalse() throws Exception {
        // Given
        when(request.getHeader("X-API-KEY")).thenReturn(null);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}