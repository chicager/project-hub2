package org.example.cache_caffeine_spring_boot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cache_caffeine_spring_boot.model.Product;
import org.example.cache_caffeine_spring_boot.repository.ProductRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    /*
    # в key = "#id" - это часть SpEL (Spring Expression Language), языка выражений Spring.
    # означает, что мы ссылаемся на параметр метода.
    #id означает "возьми значение параметра id"
    Без # Spring не поймет, что мы хотим использовать параметр метода
    Это как:
      # - это как указатель "посмотри сюда"
      #id - "посмотри значение параметра id"
      Без # это было бы как "посмотри на буквы id"
    Примеры использования:
      // Используем параметр метода
      @Cacheable(value = "products", key = "#id")
      // Используем несколько параметров
      @Cacheable(value = "products", key = "#name + '-' + #price")
      // Используем поле объекта
      @Cacheable(value = "products", key = "#product.id")
      // Используем результат метода
      @Cacheable(value = "products", key = "#product.getId()")
    Поэтому # - это обязательный синтаксис SpEL для ссылки на параметры метода или другие объекты в контексте выполнения.
    */
    // Пример 1: Простое кеширование результата метода
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
    }

    /*
    Это работает так:
      Когда кто-то запрашивает продукты с минимальной ценой:
        Сначала проверяется условие #minPrice > 0
        Если цена меньше или равна 0:
          Метод выполняется
          Результат НЕ сохраняется в кеш
        Если цена больше 0:
          Проверяется кеш
          Если в кеше есть данные - берем оттуда
          Если нет - выполняем метод и сохраняем в кеш
    Это как:
      condition - это как охранник на входе
      Если цена > 0:
        Охранник пропускает
        Можно использовать кеш
      Если цена ≤ 0:
        Охранник не пропускает
        Кеш не используется
        Всегда ходим в базу данных
    Зачем это нужно:
      Не кешировать некорректные запросы
      Экономить память кеша
      Всегда получать актуальные данные для некорректных запросов
    В данном случае ключ не указан явно, поэтому Spring использует значение параметра minPrice как ключ по умолчанию.
    */
    // Пример 2: Кеширование списка с условием
    @Cacheable(value = "productList", condition = "#minPrice > 0")
    public List<Product> getProductsByMinPrice(Double minPrice) {
        log.info("Fetching products with min price: {}", minPrice);
        return productRepository.findAll().stream()
                .filter(product -> product.getPrice() >= minPrice)
                .toList();
    }

    /*
    @CachePut - это аннотация, которая:
      Всегда выполняет метод
      Обновляет кеш результатом выполнения
    Работает так:
      Метод ВСЕГДА выполняется (в отличие от @Cacheable)
      Результат сохраняется в кеш
      Кеш обновляется новыми данными
    Пример работы:
       // Первый вызов
       updateProduct(product1)  // id = 1
       // 1. Выполняется метод
       // 2. Обновляется база
       // 3. В кеш "products" по ключу 1 сохраняется product1

       // Второй вызов с тем же id
       updateProduct(product2)  // id = 1
       // 1. Выполняется метод
       // 2. Обновляется база
       // 3. В кеш "products" по ключу 1 обновляется на product2
    Это как:
      @Cacheable - это как "сначала проверь записную книжку"
      @CachePut - это как "всегда записывай новое в записную книжку"
    Поэтому @CachePut используется для:
      Обновления данных
      Синхронизации кеша с базой данных
      Обеспечения актуальности данных в кеше
    Если произойдет ошибка во время выполнения метода, то:
      Метод не выполнится до конца
      Кеш НЕ будет обновлен
      В кеше останутся старые данные
    Поэтому:
      Кеш обновляется только при успешном выполнении метода
      При ошибке кеш остается в предыдущем состоянии
      Это может привести к расхождению между кешем и базой данных
    Для решения этой проблемы можно:
      Добавить обработку ошибок
      Использовать @Transactional для отката изменений
      Очищать кеш при ошибках
    */
    // Пример 3: Обновление кеша при изменении данных
    @CachePut(value = "products", key = "#product.id")
    @Transactional //В этом примере избыточна, но в реальной работе надо использовать
    public Product updateProduct(Product product) {
        log.info("Updating product: {}", product);
        return productRepository.save(product);
    }

    /*
    @CacheEvict работает так:
      Выполняется метод удаления из базы данных
      Если удаление успешно:
        Удаляется запись из кеша по указанному ключу
        В нашем случае удаляется запись с ключом id из кеша "products"
    Важные моменты:
      Если произойдет ошибка при удалении из базы:
        Кеш не будет очищен
        Данные останутся в кеше
      Если продукт не существует в базе:
        Метод все равно выполнится
        Кеш будет очищен
      Поэтому @CacheEvict используется для:
        Синхронизации кеша с базой данных
        Очистки устаревших данных
        Поддержания согласованности данных
    */
    // Пример 4: Очистка кеша при удалении
    @CacheEvict(value = "products", key = "#id")
    @Transactional //В этом примере избыточна, но в реальной работе надо использовать
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        productRepository.deleteById(id);
    }

    /*
    @CacheEvict здесь работает так:
      value = {"products", "productList"} - очищает два кеша:
        Кеш "products" (где хранятся отдельные продукты)
        Кеш "productList" (где хранятся списки продуктов)
      allEntries = true - очищает ВСЕ записи в этих кешах, а не только по конкретному ключу
    Пример работы:
      // Предположим, в кешах есть данные:
      // products: {1: product1, 2: product2}
      // productList: {100: [product1, product2], 200: [product2]}

      createProduct(newProduct)
      // 1. Сохраняется новый продукт в базу
      // 2. Очищается весь кеш "products"
      // 3. Очищается весь кеш "productList"
    Почему так:
      При создании нового продукта:
        Меняется список всех продуктов
        Меняются все фильтрованные списки
        Поэтому нужно очистить все кеши
      При следующем запросе:
        Кеши будут пусты
        Данные будут заново загружены из базы
        Кеши будут содержать актуальные данные

    Хотя можно написать так:
      @CachePut(value = "products", key = "#product.id")
      @CacheEvict(value = "productList", allEntries = true)
      public Product createProduct(Product product) {
        log.info("Creating new product: {}", product);
        return productRepository.save(product);
      }
    В Spring можно использовать несколько аннотаций кеширования на одном методе.
    Это работает так:
      Сначала выполняются все @CacheEvict
      Затем выполняется метод
      После успешного выполнения метода выполняются все @CachePut
    В нашем случае:
      Очищается кеш "productList"
      Сохраняется продукт в базу
      Новый продукт добавляется в кеш "products"
    Поэтому такой подход:
      Полностью поддерживается Spring
      Является хорошей практикой
      Позволяет гибко управлять кешем
    И порядок выполнения именно такой:
      Сначала @CacheEvict
      Затем выполнение метода
      Потом @CachePut
    */
    // Пример 5: Очистка всех кешей при создании нового продукта
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    @Transactional //В этом примере избыточна, но в реальной работе надо использовать
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product);
        return productRepository.save(product);
    }

    /*
    Работает так:
      При первом вызове с конкретными параметрами (например, name="Phone", price=1000):
      Создается ключ кеша: "Phone-1000"
      Проверяется кеш по этому ключу
      В кеше ничего нет
      Выполняется метод
      Результат сохраняется в кеш
    При следующем вызове с теми же параметрами:
      Создается тот же ключ: "Phone-1000"
      Проверяется кеш
      Данные есть в кеше
      Метод НЕ выполняется
      Возвращаются данные из кеша
    Если параметры разные - будет новый ключ кеша
    */
    // Пример 6: Кеширование с использованием SpEL
    @Cacheable(value = "products", key = "#name + '-' + #price")
    public Optional<Product> findByNameAndPrice(String name, Double price) {
        log.info("Finding product by name: {} and price: {}", name, price);
        return productRepository.findAll().stream()
                .filter(p -> p.getName().equals(name) && p.getPrice().equals(price))
                .findFirst();
    }

    /*
    cacheManager - это бин Spring, который управляет всеми кешами в приложении
    getCacheNames() возвращает коллекцию Collection<String> с именами всех зарегистрированных кешей

    Objects.requireNonNull() - проверяет, что полученный кеш не null
    Если кеш окажется null, будет выброшено исключение NullPointerException
    Это защитный механизм, чтобы не работать с несуществующими кешами
    */
    /**
     * Программно очищает все кеши
     */
    public void clearAllCaches() {
        log.info("Clearing all caches");
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    /**
     * Программно очищает конкретный кеш
     */
    public void clearCache(String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
    }

    /**
     * Программно добавляет значение в кеш
     */
    public void putInCache(String cacheName, Object key, Object value) {
        log.info("Putting value in cache: {} with key: {}", cacheName, key);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, value);
    }

    /**
     * Программно получает значение из кеша
     */
    public Object getFromCache(String cacheName, Object key) {
        log.info("Getting value from cache: {} with key: {}", cacheName, key);
        return Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key)
                .get();
    }

    /**
     * Программно удаляет значение из кеша
     */
    public void evictFromCache(String cacheName, Object key) {
        log.info("Evicting from cache: {} with key: {}", cacheName, key);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).evict(key);
    }

    /**
     * Получает все имена кешей
     */
    public List<String> getCacheNames() {
        return cacheManager.getCacheNames().stream().toList();
    }


    /*
    Метод для получения всех значений из кеша. В Spring Cache API нет прямого метода для получения всех значений,
    но мы можем это реализовать через Caffeine Cache:
    */

    /**
     * Получает все значения из указанного кеша
     * @param cacheName имя кеша
     * @return Map с ключами и значениями из кеша
     */
    public Map<Object, Object> getCacheContents(String cacheName) {
        log.info("Getting all values from cache: {}", cacheName);

        var springCache = cacheManager.getCache(cacheName);
        if (springCache == null) {
            throw new IllegalArgumentException("Cache '" + cacheName + "' not found");
        }

        // Получаем нативный Caffeine кеш
        var caffeineCache = ((CaffeineCache) springCache).getNativeCache();
        return caffeineCache.asMap();
    }


    /*
    Важные моменты:
      Мы используем нативный Caffeine кеш для получения всех значений,
      так как Spring Cache API не предоставляет такой функциональности
      Метод asMap() возвращает неизменяемую копию содержимого кеша
      Статистика кеша помогает понять, насколько эффективно работает кеширование.
    Этот метод особенно полезен для:
      Отладки
      Мониторинга
      Проверки корректности работы кеша
      Анализа эффективности кеширования
    */

    /**
     * Получает статистику использования кеша
     * @param cacheName имя кеша
     * @return информация о кеше
     */
    public Map<String, Object> getCacheStats(String cacheName) {
        log.info("Getting stats for cache: {}", cacheName);

        var springCache = cacheManager.getCache(cacheName);
        if (springCache == null) {
            throw new IllegalArgumentException("Cache '" + cacheName + "' not found");
        }

        var caffeineCache = ((CaffeineCache) springCache).getNativeCache();
        var stats = caffeineCache.stats();

        return Map.of(
                "hitCount", stats.hitCount(),
                "missCount", stats.missCount(),
                "evictionCount", stats.evictionCount(),
                "estimatedSize", caffeineCache.estimatedSize()
        );
    }
}
