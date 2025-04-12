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

    //Кэш нужно использовать для часто запрашиваемых данных
    /*
    CacheName (или value в аннотации @Cacheable) нужен для логического разделения разных типов кэшированных данных.
    Это как разные "области" или "пространства имен" в кэше.
    */
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

    /*
    #result - это специальная переменная в Spring Expression Language (SpEL),
    которая представляет собой результат выполнения метода.
    В данном случае:
      Метод getProductWithUnless возвращает объект Product
      #result ссылается на этот возвращаемый объект
      #result.price обращается к полю price возвращаемого объекта
      unless = "#result.price > 1000" означает: "не кэшировать результат, если цена продукта больше 1000"
    Таким образом, когда метод выполняется:
      Сначала проверяется кэш по ключу (в данном случае по id)
      Если значение найдено в кэше - оно возвращается сразу, без выполнения метода
      Если значение НЕ найдено в кэше:
        Выполняется метод getProductWithUnless
        Получается результат
        Проверяется условие unless
        Если условие unless истинно (цена > 1000) - результат НЕ сохраняется в кэш
        Если условие unless ложно (цена ≤ 1000) - результат сохраняется в кэш
      Возвращается результат
    Таким образом, проверка кэша происходит ДО выполнения метода, а условие unless проверяется
    только если значение не было найдено в кэше и метод был выполнен.
    Это полезно, когда мы хотим кэшировать только определенные результаты,
    основываясь на значениях возвращаемого объекта.
    */
    // 3. Кэширование с unless условием (не кэшируем если цена > 1000)
    @Cacheable(value = "products", key = "#id", unless = "#result.price > 1000")
    public Product getProductWithUnless(Long id) {
        log.info("Fetching product with unless for id: {}", id);
        simulateSlowOperation();
        return productDatabase.get(id);
    }

    /*
    Метод updateProduct ВСЕГДА выполняется (в отличие от @Cacheable)
    После выполнения метода:
      Результат (обновленный продукт) сохраняется в кэш
      Ключ кэша формируется из #product.id
      Значение кэша - это возвращаемый объект Product
    Таким образом, @CachePut используется для обновления кэша, когда мы изменяем данные.
    Это важно для поддержания согласованности между данными в кэше и в основном хранилище данных.
    В нашем случае:
      Вызывается метод updateProduct
      Продукт обновляется в productDatabase
      Обновленный продукт автоматически сохраняется в Redis кэш
      Возвращается обновленный продукт
    Это отличается от @Cacheable, где сначала проверяется кэш,
    и метод может не выполниться, если данные уже есть в кэше.
    */
    // 4. Обновление кэша при изменении данных
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        log.info("Updating product: {}", product);
        simulateSlowOperation();
        productDatabase.put(product.getId(), product);
        return product;
    }

    /*
    Метод deleteProduct ВСЕГДА выполняется
    После выполнения метода:
      Удаляется запись из кэша по указанному ключу (#id)
      В данном случае удаляется продукт с указанным id из Redis кэша
    В нашем конкретном случае:
      Вызывается метод deleteProduct
      Продукт удаляется из productDatabase
      Запись с этим id автоматически удаляется из Redis кэша
    @CacheEvict используется для удаления данных из кэша, когда мы удаляем или изменяем данные
    в основном хранилище. Это важно для поддержания согласованности данных между кэшем
    и основным хранилищем.
    Есть также вариант с параметром allEntries = true (как в методе clearCache),
    который удаляет все записи из указанного кэша, а не только по конкретному ключу.
    */
    // 5. Удаление из кэша при удалении данных
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        simulateSlowOperation();
        productDatabase.remove(id);
    }

    /*
    Метод clearCache ВСЕГДА выполняется
    После выполнения метода:
      Удаляются ВСЕ записи из кэша с именем "products"
      Параметр allEntries = true указывает, что нужно удалить все записи, а не только по конкретному ключу
    В нашем конкретном случае:
      Вызывается метод clearCache
      Все продукты удаляются из Redis кэша
      При следующем запросе любого продукта, данные будут заново загружены из productDatabase
    Этот метод полезен, когда нам нужно полностью очистить кэш, например:
      При массовом обновлении данных
      При подозрении на несогласованность данных в кэше
      При необходимости принудительного обновления всех данных из основного хранилища
    */
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
