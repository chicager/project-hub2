package org.example.work_with_properties_spring_boot.service;

import org.example.work_with_properties_spring_boot.config.AppProperties;
import org.example.work_with_properties_spring_boot.config.advanced.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationDemoServiceTest {

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private ConfigurationDemoService configurationDemoService;

    @BeforeEach
    void setUp() {
        // Setup basic properties
        when(appProperties.getName()).thenReturn("Test App");
        when(appProperties.getVersion()).thenReturn("1.0.0");

        // Setup endpoints
        EndpointConfig endpointConfig = new EndpointConfig();
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("users", "/api/users");
        endpoints.put("orders", "/api/orders");
        endpointConfig.setEndpoints(endpoints);
        when(appProperties.getEndpoints()).thenReturn(endpointConfig);

        // Setup feature flags
        FeatureFlags featureFlags = new FeatureFlags();
        Map<String, Boolean> flags = new HashMap<>();
        flags.put("newUI", true);
        flags.put("beta", false);
        featureFlags.setFeatureFlags(flags);
        when(appProperties.getFeatureFlags()).thenReturn(featureFlags);

        // Setup retry config
        RetryConfig retryConfig = new RetryConfig();
        List<Integer> delays = Arrays.asList(1000, 2000, 3000);
        retryConfig.setDelays(delays);
        when(appProperties.getRetryConfig()).thenReturn(retryConfig);

        // Setup cache config
        CacheConfig cacheConfig = new CacheConfig();
        Map<String, Integer> ttlSettings = new HashMap<>();
        ttlSettings.put("users", 300);
        ttlSettings.put("products", 600);
        cacheConfig.setTtlSettings(ttlSettings);
        when(appProperties.getCache()).thenReturn(cacheConfig);

        // Setup CORS config
        CorsSettings corsSettings = new CorsSettings();
        Map<String, CorsSettings.CorsConfig> settings = new HashMap<>();
        CorsSettings.CorsConfig corsConfig = new CorsSettings.CorsConfig();
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
        corsConfig.setMaxAge(3600);
        settings.put("http://localhost:3000", corsConfig);
        corsSettings.setSettings(settings);
        when(appProperties.getCors()).thenReturn(corsSettings);

        // Setup Environment config
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        Map<String, EnvironmentConfig.EnvSettings> envSettings = new HashMap<>();
        EnvironmentConfig.EnvSettings devSettings = new EnvironmentConfig.EnvSettings();
        devSettings.setLoggingLevel("DEBUG");
        devSettings.setMockServices(true);
        envSettings.put("dev", devSettings);
        environmentConfig.setEnvironmentSpecific(envSettings);
        when(appProperties.getEnvironmentSpecific()).thenReturn(environmentConfig);
    }

    @Test
    void demonstrateConfigShouldLogAllConfigurations() {
        // Act
        configurationDemoService.demonstrateConfig();

        // Assert - verify that all necessary methods were called
        verify(appProperties).getName();
        verify(appProperties).getVersion();
        verify(appProperties).getEndpoints();
        verify(appProperties).getFeatureFlags();
        verify(appProperties).getRetryConfig();
        verify(appProperties).getCache();
        verify(appProperties).getCors();
        verify(appProperties).getEnvironmentSpecific();
    }

    @Test
    void initShouldCallDemonstrateConfig() {
        // Act
        configurationDemoService.init();

        // Assert
        verify(appProperties).getName();
        verify(appProperties).getVersion();
        // Verify other method calls as needed
    }
}