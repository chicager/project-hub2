package org.example.cache_redis_spring_boot.service;

import org.example.cache_redis_spring_boot.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Описание этих аннотаций есть в классе ProductControllerTest
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ProductServiceTest {

    //Описание этого кода есть в классе ProductControllerTest
    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    //Описание этого кода есть в классе ProductControllerTest
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

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
    void testGetProduct() {
        // Первый вызов - должен загрузить из базы
        Product firstCall = productService.getProduct(1L);
        assertNotNull(firstCall);
        assertEquals("Test Product", firstCall.getName());

        // Второй вызов - должен взять из кэша
        Product secondCall = productService.getProduct(1L);
        assertNotNull(secondCall);
        assertEquals("Test Product", secondCall.getName());
    }

    @Test
    void testGetProductWithUnless() {
        // Создаем продукт с ценой > 1000
        Product expensiveProduct = Product.builder()
                .id(2L)
                .name("Expensive Product")
                .price(new BigDecimal("2000.00"))
                .build();
        productService.updateProduct(expensiveProduct);

        // Вызов должен выполниться дважды, так как цена > 1000
        Product firstCall = productService.getProductWithUnless(2L);
        Product secondCall = productService.getProductWithUnless(2L);
        assertNotNull(firstCall);
        assertNotNull(secondCall);
    }

    @Test
    void testUpdateProduct() {
        // Обновляем продукт
        testProduct.setName("Updated Name");
        Product updated = productService.updateProduct(testProduct);

        // Проверяем, что обновление произошло
        assertEquals("Updated Name", updated.getName());

        // Проверяем, что кэш обновился
        Product fromCache = productService.getProduct(1L);
        assertEquals("Updated Name", fromCache.getName());
    }

    @Test
    void testDeleteProduct() {
        // Удаляем продукт
        productService.deleteProduct(1L);

        // Проверяем, что продукт удален из кэша
        Product fromCache = productService.getProduct(1L);
        assertNull(fromCache);
    }

    @Test
    void testGetAllProducts() {
        // Добавляем еще продукты
        for (int i = 2; i <= 5; i++) {
            Product product = Product.builder()
                    .id((long) i)
                    .name("Product " + i)
                    .price(new BigDecimal("100.00"))
                    .build();
            productService.updateProduct(product);
        }

        // Получаем первую страницу
        List<Product> firstPage = productService.getAllProducts(0, 2);
        assertEquals(2, firstPage.size());

        // Получаем вторую страницу
        List<Product> secondPage = productService.getAllProducts(1, 2);
        assertEquals(2, secondPage.size());

        // Проверяем, что страницы разные
        assertNotEquals(firstPage.get(0).getId(), secondPage.get(0).getId());
    }

    @Test
    void testClearCache() {
        // Первый вызов - загружает из сервиса
        Product firstCall = productService.getProduct(1L);
        assertNotNull(firstCall);

        // Очищаем кэш
        productService.clearCache();

        // Второй вызов - должен снова загрузить из сервиса
        Product secondCall = productService.getProduct(1L);
        assertNotNull(secondCall);

        // Проверяем, что это тот же продукт
        assertEquals(firstCall.getId(), secondCall.getId());
        assertEquals(firstCall.getName(), secondCall.getName());
    }
}