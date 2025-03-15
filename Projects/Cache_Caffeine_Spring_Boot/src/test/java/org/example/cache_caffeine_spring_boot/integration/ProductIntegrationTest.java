package org.example.cache_caffeine_spring_boot.integration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.cache_caffeine_spring_boot.model.Product;
import org.example.cache_caffeine_spring_boot.repository.ProductRepository;
import org.example.cache_caffeine_spring_boot.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/*
@SpringBootTest - Загружает весь контекст приложения: Когда запускается тест с этой аннотацией, Spring Boot загружает
все компоненты твоего приложения (например, сервисы, репозитории, конфигурации) так, как если бы запускалось
само приложение. Это значит, что все бины (компоненты) будут доступны в тесте, и можно будет их использовать.
*/
@SpringBootTest()
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Пересоздаем кеши для сброса статистики
        if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
            // Получаем новый Caffeine builder с чистой статистикой
            Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                    .expireAfterWrite(60, TimeUnit.MINUTES)
                    .initialCapacity(100)
                    .maximumSize(500)
                    .recordStats();

            // Обновляем конфигурацию кеш менеджера
            caffeineCacheManager.setCaffeine(caffeine);

            // Очищаем существующие кеши
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(100.0);
        testProduct.setStock(10);
    }

    @Test
    void cacheWorkflow_ShouldCacheAndEvict() {
        // Сохраняем продукт
        Product savedProduct = productService.createProduct(testProduct);
        Long productId = savedProduct.getId();
        assertNotNull(productId);

        // Первый запрос - должен обратиться к БД
        Optional<Product> firstRequest = productService.getProductById(productId);
        assertTrue(firstRequest.isPresent());
        assertEquals("Test Product", firstRequest.get().getName());

        // Модифицируем продукт напрямую через репозиторий (в обход кеша)
        transactionTemplate.execute(status -> {
            // Создаем новый объект Product для модификации
            Product modifiedProduct = new Product();
            modifiedProduct.setId(productId);
            modifiedProduct.setName("Modified Name");
            modifiedProduct.setDescription(testProduct.getDescription());
            modifiedProduct.setPrice(testProduct.getPrice());
            modifiedProduct.setStock(testProduct.getStock());
            return productRepository.save(modifiedProduct);
        });

        // Второй запрос - должен вернуть закешированное (старое) значение
        Optional<Product> secondRequest = productService.getProductById(productId);
        assertTrue(secondRequest.isPresent());
        assertEquals("Test Product", secondRequest.get().getName()); // все еще старое имя из кеша

        // Обновляем через сервис (должно обновить кеш)
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated Name");
        updatedProduct.setDescription(testProduct.getDescription());
        updatedProduct.setPrice(testProduct.getPrice());
        updatedProduct.setStock(testProduct.getStock());
        productService.updateProduct(updatedProduct);

        // Третий запрос - должен вернуть новое значение
        Optional<Product> thirdRequest = productService.getProductById(productId);
        assertTrue(thirdRequest.isPresent());
        assertEquals("Updated Name", thirdRequest.get().getName());

        // Проверяем удаление из кеша
        productService.deleteProduct(productId);
        Optional<Product> afterDelete = productService.getProductById(productId);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void productListCache_ShouldWorkWithCondition() {
        // Создаем два продукта с разными ценами
        Product cheapProduct = new Product();
        cheapProduct.setName("Cheap Product");
        cheapProduct.setDescription("Test Description");
        cheapProduct.setPrice(100.0);
        cheapProduct.setStock(10);
        productService.createProduct(cheapProduct);

        Product expensiveProduct = new Product();
        expensiveProduct.setName("Expensive Product");
        expensiveProduct.setDescription("Test Description");
        expensiveProduct.setPrice(200.0);
        expensiveProduct.setStock(10);
        productService.createProduct(expensiveProduct);

        // Первый запрос - загружает из БД и кеширует (minPrice > 0)
        List<Product> firstRequest = productService.getProductsByMinPrice(150.0);
        assertEquals(1, firstRequest.size());
        assertEquals("Expensive Product", firstRequest.get(0).getName());

        // Модифицируем дешевый продукт напрямую через репозиторий, делая его дорогим
        transactionTemplate.execute(status -> {
            cheapProduct.setPrice(300.0);
            return productRepository.save(cheapProduct);
        });

        // Второй запрос - должен вернуть закешированный результат (только один дорогой продукт)
        List<Product> secondRequest = productService.getProductsByMinPrice(150.0);
        assertEquals(1, secondRequest.size());
        assertEquals("Expensive Product", secondRequest.get(0).getName());

        // Очищаем кеш
        productService.clearCache("productList");

        // Третий запрос - должен загрузить актуальные данные (два дорогих продукта)
        List<Product> thirdRequest = productService.getProductsByMinPrice(150.0);
        assertEquals(2, thirdRequest.size());
    }

    @Test
    void cacheStatistics_ShouldTrackHitsAndMisses() {
        // Создаем продукт через репозиторий (в обход очистки кеша)
        Product savedProduct = transactionTemplate.execute(status ->
                productRepository.save(testProduct)
        );
        assertNotNull(savedProduct);

        // Первый запрос - miss (загрузка в кеш)
        Optional<Product> firstRequest = productService.getProductById(savedProduct.getId());
        assertTrue(firstRequest.isPresent());

        // Проверяем статистику после первого запроса
        var statsAfterMiss = productService.getCacheStats("products");
        System.out.println("Stats after miss: " + statsAfterMiss);

        // Проверяем, что статистика существует
        assertNotNull(statsAfterMiss);
        assertTrue(statsAfterMiss.containsKey("hitCount"));
        assertTrue(statsAfterMiss.containsKey("missCount"));

        assertEquals(0L, statsAfterMiss.get("hitCount"), "Should have no cache hits initially");
        assertEquals(1L, statsAfterMiss.get("missCount"), "Should have one cache miss");

        // Второй запрос - должен быть hit (чтение из кеша)
        Optional<Product> secondRequest = productService.getProductById(savedProduct.getId());
        assertTrue(secondRequest.isPresent());

        // Проверяем статистику после второго запроса
        var statsAfterHit = productService.getCacheStats("products");
        System.out.println("Stats after hit: " + statsAfterHit);
        assertEquals(1L, statsAfterHit.get("hitCount"), "Should have one cache hit");
        assertEquals(1L, statsAfterHit.get("missCount"), "Should still have one cache miss");

        // Третий запрос - тоже должен быть hit
        Optional<Product> thirdRequest = productService.getProductById(savedProduct.getId());
        assertTrue(thirdRequest.isPresent());

        // Проверяем финальную статистику
        var finalStats = productService.getCacheStats("products");
        System.out.println("Final stats: " + finalStats);
        assertEquals(2L, finalStats.get("hitCount"), "Should have exactly 2 cache hits");
        assertEquals(1L, finalStats.get("missCount"), "Should have exactly 1 cache miss");
    }

    @Test
    void cacheName_ShouldBeAvailable() {
        List<String> cacheNames = productService.getCacheNames();
        assertTrue(cacheNames.contains("products"));
        assertTrue(cacheNames.contains("productList"));
    }

    @Test
    void cacheContents_ShouldBeAccessible() {
        // Создаем продукт
        Product savedProduct = productService.createProduct(testProduct);

        // Загружаем в кеш
        productService.getProductById(savedProduct.getId());

        // Проверяем содержимое кеша
        var cacheContents = productService.getCacheContents("products");
        assertFalse(cacheContents.isEmpty());
        assertTrue(cacheContents.containsKey(savedProduct.getId()));
    }
}