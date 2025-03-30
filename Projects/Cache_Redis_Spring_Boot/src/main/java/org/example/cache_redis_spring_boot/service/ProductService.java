package org.example.cache_redis_spring_boot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.cache_redis_spring_boot.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ProductService {

    // Имитация базы данных
    private final Map<Long, Product> productDatabase = new ConcurrentHashMap<>();

    // 1. Простое кэширование с автоматическим обновлением
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        log.info("Fetching product with id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    // 2. Кэширование с условным условием
    @Cacheable(value = "products", key = "#id", condition = "#id > 0")
    public Product getProductWithCondition(Long id) {
        log.info("Fetching product with condition for id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    // 3. Кэширование с unless условием (не кэшируем если цена > 1000)
    @Cacheable(value = "products", key = "#id", unless = "#result.price > 1000")
    public Product getProductWithUnless(Long id) {
        log.info("Fetching product with unless for id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    // 4. Обновление кэша при изменении данных
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        log.info("Updating product: {}", product);
        simulateSlowOperation();
        productDatabase.put(product.getId(), product);
        return product;
    }

    // 5. Удаление из кэша при удалении данных
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        simulateSlowOperation();
        productDatabase.remove(id);
    }

    // 6. Удаление всего кэша
    @CacheEvict(value = "products", allEntries = true)
    public void clearCache() {
        log.info("Clearing all product cache");
    }

    // 7. Кэширование списка с составным ключом
    @Cacheable(value = "productLists", key = "'all-products-' + #page + '-' + #size")
    public List<Product> getAllProducts(int page, int size) {
        log.info("Fetching all products with pagination: page={}, size={}", page, size);
        simulateSlowOperation();
        List<Product> products = new ArrayList<>(productDatabase.values());
        int start = page * size;
        int end = Math.min(start + size, products.size());
        return products.subList(start, end);
    }

    // 8. Кэширование с TTL (время жизни)
    @Cacheable(value = "hotProducts", key = "#id")
    public Product getHotProduct(Long id) {
        log.info("Fetching hot product with id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    /*
    Давайте разберем разницу между key и keyGenerator:
      key - простое определение ключа:
        // Простой случай с одним параметром
        @Cacheable(value = "products", key = "#id")
        public Product getProduct(Long id) { ... }
      keyGenerator - использование генератора ключей:
        @Cacheable(value = "products", keyGenerator = "productCacheKeyGenerator")
        public Product getProduct(Long id) { ... }
      Когда что использовать:
        Используйте key когда:
          Простая логика формирования ключа
          Ключ формируется из параметров метода
          Нужны SpEL выражения
      Используйте keyGenerator когда:
        Сложная логика формирования ключа
          Повторяющаяся логика в разных методах
          Нужна дополнительная обработка параметров
      Важные моменты:
        Нельзя использовать key и keyGenerator одновременно
        key использует SpEL выражения
        keyGenerator требует реализации интерфейса KeyGenerator
        При отсутствии обоих параметров используется SimpleKeyGenerator (по умолчанию)
      Выбор между key и keyGenerator зависит от:
        Сложности логики формирования ключа
        Потребности в переиспользовании логики
        Необходимости дополнительной обработки
        Требований к форматированию ключей
    */
    // 9. Кэширование с использованием общего CacheKeyGenerator
    @Cacheable(value = "products", keyGenerator = "cacheKeyGenerator")
    public Product getProductWithCustomKey(Long id, String region) {
        log.info("Fetching product with custom key for id: {} and region: {}", id, region);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    // 10. Кэширование с использованием специфичного ProductCacheKeyGenerator
    @Cacheable(value = "products", keyGenerator = "productCacheKeyGenerator")
    public Product getProductWithProductKey(Long id) {
        log.info("Fetching product with product key for id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    // 11. Кэширование списка с использованием ProductCacheKeyGenerator
    @Cacheable(value = "productLists", keyGenerator = "productCacheKeyGenerator")
    public List<Product> getAllProductsWithProductKey(int page, int size) {
        log.info("Fetching all products with product key: page={}, size={}", page, size);
        simulateSlowOperation();
        List<Product> products = new ArrayList<>(productDatabase.values());
        int start = page * size;
        int end = Math.min(start + size, products.size());
        return products.subList(start, end);
    }

    private void simulateSlowOperation() {
        try {
            Thread.sleep(1000); // Имитация медленной операции
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
