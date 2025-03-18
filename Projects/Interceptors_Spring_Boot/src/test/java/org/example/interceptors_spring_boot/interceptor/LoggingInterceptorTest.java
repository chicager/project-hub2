package org.example.interceptors_spring_boot.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingInterceptorTest {

    @InjectMocks
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    void preHandle_ShouldSetStartTimeAndReturnTrue() throws Exception {
        // When
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(request).setAttribute(eq("startTime"), any(Long.class));
    }

    @Test
    void postHandle_ShouldLogRequestInfo() throws Exception {
        // Given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/tasks");

        // When
        loggingInterceptor.postHandle(request, response, handler, null);

        // Then
        verify(request, times(1)).getMethod();
        verify(request, times(1)).getRequestURI();
    }

    @Test
    void afterCompletion_ShouldCalculateExecutionTime() throws Exception {
        // Given
        long startTime = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(startTime);

        // When
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Then
        verify(request, times(1)).getAttribute("startTime");
    }

    @Test
    void afterCompletion_WithException_ShouldLogError() throws Exception {
        // Given
        Exception exception = new RuntimeException("Test exception");
        long startTime = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(startTime);

        // When
        loggingInterceptor.afterCompletion(request, response, handler, exception);

        // Then
        verify(request, times(1)).getAttribute("startTime");
    }
}