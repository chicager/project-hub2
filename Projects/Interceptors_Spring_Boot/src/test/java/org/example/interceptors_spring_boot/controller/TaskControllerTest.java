package org.example.interceptors_spring_boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.interceptors_spring_boot.config.WebMvcConfig;
import org.example.interceptors_spring_boot.interceptor.AuthenticationInterceptor;
import org.example.interceptors_spring_boot.interceptor.LoggingInterceptor;
import org.example.interceptors_spring_boot.model.Task;
import org.example.interceptors_spring_boot.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
@WebMvcTest(TaskController.class):
  Загружает только веб-слой приложения (контроллеры, фильтры, перехватчики и т.д.)
  НЕ загружает полный контекст Spring приложения
  Автоматически настраивает MockMvc для тестирования REST endpoints
  Загружает только указанный контроллер (в нашем случае TaskController)
Почему это хорошо:
  @WebMvcTest(TaskController.class) // Загружаем только TaskController
  class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc; // Автоматически настраивается

    @MockBean
    private TaskRepository taskRepository; // Нужно явно мокировать зависимости

    // Тесты...
  }
Сравнение с полным тестом @SpringBootTest:
  @SpringBootTest // Загружает ВСЁ приложение
  @AutoConfigureMockMvc // Нужно добавлять отдельно
  class TaskControllerTest {
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
В нашем случае @WebMvcTest(TaskController.class) идеально подходит, потому что:
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
@WebMvcTest(TaskController.class)
/*
Аннотация @Import в данном случае используется для добавления необходимых компонентов в контекст тестов.
Почему это нужно:
  @WebMvcTest загружает только базовый веб-слой и тестируемый контроллер
  Но наше приложение использует дополнительные компоненты (перехватчики и конфигурацию)
  Через @Import мы явно указываем, какие дополнительные компоненты нужны для тестов
Что именно импортируется
  @Import({
    WebMvcConfig.class,           // Конфигурация Spring MVC (настройка перехватчиков)
    AuthenticationInterceptor.class, // Перехватчик для проверки API-ключа
    LoggingInterceptor.class      // Перехватчик для логирования
  })
Простая аналогия:
  @WebMvcTest - как базовая комплектация автомобиля
  @Import - как добавление дополнительных опций к базовой комплектации
Почему эти компоненты важны для тестов
  // Тест с проверкой API-ключа (нужен AuthenticationInterceptor)
  @Test
  void getAllTasks_WithoutApiKey_ShouldReturnUnauthorized() {
    mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isUnauthorized());
  }
  // Тест обычного запроса (нужны все перехватчики)
  @Test
  void getAllTasks_ShouldReturnListOfTasks() {
    mockMvc.perform(get("/api/tasks")
            .header("X-API-KEY", VALID_API_KEY)) // Проверяется AuthenticationInterceptor
            .andExpect(status().isOk());
    // LoggingInterceptor логирует запрос
  }
Что было бы без @Import:
  Перехватчики бы не работали
  Тесты на проверку API-ключа падали бы
  Логирование не работало бы
  Конфигурация MVC не применялась бы
Это важно потому что:
  Тесты максимально приближены к реальному поведению приложения
  Мы тестируем не только контроллер, но и всю инфраструктуру вокруг него
  Можем быть уверены, что перехватчики работают правильно
  Тесты остаются изолированными и контролируемыми

Хотя эта строка избыточна, можно удалить, оставил для примера, она избыточна потому что:
  В Spring Boot 3.x появилось автоматическое сканирование компонентов даже в тестах
  с @WebMvcTest. Это значит, что Spring Boot автоматически находит и подключает:
    Конфигурационные классы (@Configuration)
    Компоненты (@Component)
    Перехватчики (если они помечены как @Component)
*/
@Import({WebMvcConfig.class, AuthenticationInterceptor.class, LoggingInterceptor.class})
class TaskControllerTest {

    /*
    MockMvc позволяет симулировать http запросы и ответы (без запуска реального сервера)
    */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /*
    Если тест с @SpringBootTest или @WebMvcTest → используем @MockBean
    Если простой тест без Spring → используем @Mock
    */
    @MockBean
    private TaskRepository taskRepository;

    private static final String VALID_API_KEY = "your-secret-key";
    private static final String INVALID_API_KEY = "invalid-key";

    @Test
    void getAllTasks_ShouldReturnListOfTasks() throws Exception {
        // Given
        Task task1 = new Task(1L, "Task 1", "Description 1", Boolean.FALSE);
        Task task2 = new Task(2L, "Task 2", "Description 2", Boolean.TRUE);
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Task 2"));

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getAllTasks_WithoutApiKey_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        verify(taskRepository, never()).findAll();
    }

    @Test
    void getAllTasks_WithInvalidApiKey_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", INVALID_API_KEY))
                .andExpect(status().isUnauthorized());

        verify(taskRepository, never()).findAll();
    }

    @Test
    void getAllTasks_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .header("X-API-KEY", VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        // Given
        Task taskToCreate = new Task(null, "New Task", "New Description", Boolean.FALSE);
        Task createdTask = new Task(1L, "New Task", "New Description", Boolean.FALSE);
        when(taskRepository.save(any(Task.class))).thenReturn(createdTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskToCreate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        Task invalidTask = new Task(null, "", null, null); // null для completed

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .header("X-API-KEY", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}")) // неправильный формат для теста
                .andExpect(status().isBadRequest());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Given
        Task task = new Task(1L, "Task 1", "Description 1", Boolean.FALSE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When & Then
        mockMvc.perform(get("/api/tasks/1")
                        .header("X-API-KEY", VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Description 1"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/1")
                        .header("X-API-KEY", VALID_API_KEY))
                .andExpect(status().isNotFound());

        verify(taskRepository, times(1)).findById(1L);
    }
}