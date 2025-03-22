package org.example.work_with_properties_spring_boot.config;

import org.example.work_with_properties_spring_boot.config.advanced.CorsSettings;
import org.example.work_with_properties_spring_boot.config.advanced.EnvironmentConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/*
@ActiveProfiles("test") - Эта аннотация указывает Spring, какой профиль использовать при запуске теста
Когда установлен профиль "test":
  Spring ищет файлы конфигурации в следующем порядке:
    application.yml (базовая конфигурация)
    application-test.yml (конфигурация профиля)
  Свойства из application-test.yml перезаписывают соответствующие свойства из application.yml
  Можно комбинировать несколько профилей: @ActiveProfiles({"test", "local"})
*/
@SpringBootTest
@ActiveProfiles("test")
class AppPropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void testBasicProperties() {
        assertEquals("Test Application", appProperties.getName());
        assertEquals("1.0.0-TEST", appProperties.getVersion());
    }

    @Test
    void testFeaturesProperties() {
        assertNotNull(appProperties.getFeatures());
        assertTrue(appProperties.getFeatures().getEnabled());
        assertEquals(10, appProperties.getFeatures().getMaxItems());
    }

    @Test
    void testSecurityProperties() {
        assertNotNull(appProperties.getSecurity());
        assertEquals("test-api-key", appProperties.getSecurity().getApiKey());
        assertNotNull(appProperties.getSecurity().getAllowedOrigins());
        assertTrue(appProperties.getSecurity().getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(appProperties.getSecurity().getAllowedOrigins().contains("http://localhost:4200"));
    }

    @Test
    void testDatabaseProperties() {
        assertNotNull(appProperties.getDatabase());
        assertEquals("test-db-url", appProperties.getDatabase().getUrl());
    }

    @Test
    void testNotificationProperties() {
        assertNotNull(appProperties.getNotification());
        assertNotNull(appProperties.getNotification().getEmail());
        assertEquals("smtp.gmail.com", appProperties.getNotification().getEmail().getHost());
        assertEquals(587, appProperties.getNotification().getEmail().getPort());
        assertEquals("noreply@myapp.com", appProperties.getNotification().getEmail().getFrom());
    }

    @Test
    void testEndpointsProperties() {
        assertNotNull(appProperties.getEndpoints());
        assertNotNull(appProperties.getEndpoints().getEndpoints());
        assertEquals("/api/users", appProperties.getEndpoints().getEndpoints().get("users"));
        assertEquals("/api/orders", appProperties.getEndpoints().getEndpoints().get("orders"));
    }

    @Test
    void testFeatureFlagsProperties() {
        assertNotNull(appProperties.getFeatureFlags());
        assertTrue(appProperties.getFeatureFlags().getFeatureFlags().get("newUI"));
        assertFalse(appProperties.getFeatureFlags().getFeatureFlags().get("beta"));
    }

    @Test
    void testRetryConfigProperties() {
        assertNotNull(appProperties.getRetryConfig());
        assertEquals(3, appProperties.getRetryConfig().getDelays().size());
        assertEquals(1000, appProperties.getRetryConfig().getDelays().get(0));
        assertEquals(2000, appProperties.getRetryConfig().getDelays().get(1));
        assertEquals(3000, appProperties.getRetryConfig().getDelays().get(2));
    }

    @Test
    void testCacheProperties() {
        assertNotNull(appProperties.getCache());
        assertEquals(300, appProperties.getCache().getTtlSettings().get("users"));
        assertEquals(600, appProperties.getCache().getTtlSettings().get("products"));
    }

    @Test
    void testCorsProperties() {
        assertNotNull(appProperties.getCors(), "cors should not be null");
        System.out.println("CORS object: " + appProperties.getCors());
        assertNotNull(appProperties.getCors().getSettings(), "cors settings should not be null");
        System.out.println("CORS settings: " + appProperties.getCors().getSettings());
        CorsSettings.CorsConfig corsSettings = appProperties.getCors().getSettings().get("http://localhost:3000");
        assertNotNull(corsSettings, "cors settings for http://localhost:3000 should not be null");
        assertNotNull(corsSettings.getAllowedMethods(), "allowed methods should not be null");
        assertTrue(corsSettings.getAllowedMethods().contains("GET"));
        assertTrue(corsSettings.getAllowedMethods().contains("POST"));
        assertEquals(3600, corsSettings.getMaxAge());
    }

    @Test
    void testEnvironmentSpecificProperties() {
        assertNotNull(appProperties.getEnvironmentSpecific());
        EnvironmentConfig.EnvSettings devSettings = appProperties.getEnvironmentSpecific().getEnvironmentSpecific().get("dev");
        assertNotNull(devSettings);
        assertEquals("DEBUG", devSettings.getLoggingLevel());
        assertTrue(devSettings.isMockServices());
    }
}
