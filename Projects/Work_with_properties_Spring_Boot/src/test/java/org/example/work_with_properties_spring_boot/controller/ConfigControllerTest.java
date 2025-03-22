package org.example.work_with_properties_spring_boot.controller;

import org.example.work_with_properties_spring_boot.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
@WebMvcTest(ConfigController.class):
  Загружает только веб-слой приложения (контроллеры, фильтры, перехватчики и т.д.)
  НЕ загружает полный контекст Spring приложения
  Автоматически настраивает MockMvc для тестирования REST endpoints
  Загружает только указанный контроллер (в нашем случае ConfigController)
*/
@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppProperties appProperties;

    @Test
    void getConfigShouldReturnConfiguration() throws Exception {
        // Arrange
        when(appProperties.getName()).thenReturn("Test App");
        when(appProperties.getVersion()).thenReturn("1.0.0");

        // Act & Assert
        mockMvc.perform(get("/api/config/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test App"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
