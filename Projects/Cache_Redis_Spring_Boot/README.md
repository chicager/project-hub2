# Пример работы с кэшем Redis в Spring Boot

------------
## Общее инфо
- В pom.xml подробно расписаны значения свойств
- В application.yml подробно расписано про логгирование
- Расписано как работает application-test.yml
- В CacheKeyGenerator расписано на примере как лучше генерировать ключи
- Подробно расписан конфиг Redis в классе RedisConfig
- Используются Testcontainers для тестирования
  - Для Testcontainers должен быть запущен локально docker, после прохождения тестов надо вручную удалить 2 образа
- Ниже описано для чего нужен self при работе с кэшем
- Ниже описаны Best Practices Redis'a
- Ниже описан SimpleKeyGenerator (стандартный генератор ключей в Spring Cache)

------------
## Запуск Redis

#### Папка resources/Redis:
- **redis-server.exe** - запускает Redis (без запуска редиса он, соответственно, работать не будет. Так же можно на dockerhub найти образ и запустить:
    docker run --name redis-cache -p 6379:6379 -d redis:latest)
  - Redis для Windows по умолчанию настроен с включенной персистентностью (RDB - Redis Database). Redis сохраняет снапшот данных в файл dump.rdb, который находится в директории установки Redis.
    Т. е. после перезапуска Redis данные остаются, можно очистить с помощью команды FLUSHALL в redis-cli.exe

## Инструменты работы с Redis

#### Папка resources/Redis:
- **redis-cli.exe** - cli для работы с Redis
  - **Полезные команды:**
    - _KEYS *_ - посмотреть все ключи
    - _GET key_ - получить значение по ключу
    - _FLUSHALL_ - удалить все данные
- **AnotherRedisDesktopManager** - утилита для просмотра базы Redis
  - Настройка соединения: 127.0.0.1:6379
  - оф. ресурсы - https://goanother.com/ , https://github.com/qishibo/AnotherRedisDesktopManager

#### Дополнительные инструменты работы с Redis:
- В IntelliJ Idea есть плагин **Redis Helper**, можно через него смотреть данные в Redis. Там можно смотреть кэши, ключи в них, и данные по этим ключам (можно выбрать JSON формат для удобства показа)
    - Настраиваем подключение (localhost:6379 (это дефолтный порт Redis'а)), и нам покажет базы редиса.
      Redis по умолчанию поддерживает 16 логических баз данных (от 0 до 15), пронумерованных от DB0 до DB15.
    - Но поскольку мы используем Spring Boot с Redis для кэширования, все данные будут находиться в DB0, 
      так как это база данных по умолчанию, если не указано иное в конфигурации.
- Есть официальная Redis утилита **RedisInsight**, можно найти на dockerhub и запустить в контейнере <br /><br />

------------------

# Spring Boot Redis Cache Demo Project

Демонстрационный проект, показывающий различные способы использования Redis для кэширования в Spring Boot приложении.

## Описание проекта
Проект демонстрирует различные стратегии кэширования с использованием Redis в Spring Boot приложении. Реализован простой REST API для работы с продуктами, где каждая операция показывает разные аспекты кэширования.

## Функциональность

### REST Endpoints (лежат в папке src/main/http)

#### Получение продуктов
- `GET /api/products/{id}` - получение продукта по ID (базовое кэширование)
- `GET /api/products/conditional/{id}` - получение с условным кэшированием
- `GET /api/products/unless/{id}` - получение с unless условием
- `GET /api/products/list?page={page}&size={size}` - получение списка с пагинацией
- `GET /api/products/hot/{id}` - получение "горячих" продуктов (с отдельным TTL)

#### Управление продуктами
- `PUT /api/products/{id}` - обновление продукта (с обновлением кэша)
- `DELETE /api/products/{id}` - удаление продукта (с удалением из кэша)
- `DELETE /api/products/cache` - очистка всего кэша

### Примеры кэширования

1. Базовое кэширование:
```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id)
```

2. Условное кэширование:
```java
@Cacheable(value = "products", key = "#id", condition = "#id > 0")
public Product getProductWithCondition(Long id)
```

3. Unless условие:
```java
@Cacheable(value = "products", key = "#id", unless = "#result.price > 1000")
public Product getProductWithUnless(Long id)
```

## Конфигурация Redis

### application.yml
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 час
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "cache:"
```

## Тестирование
Проект включает интеграционные тесты с использованием TestContainers:
```java
@Container
private static final GenericContainer<?> redis =
    new GenericContainer<>(DockerImageName.parse("redis:latest"))
        .withExposedPorts(6379);
```

## Особенности реализации

### Генераторы ключей кэша
- Простой генератор ключей
- Продуктовый генератор ключей с дополнительной логикой

### Стратегии кэширования
- TTL (время жизни кэша)
- Условное кэширование
- Селективная инвалидация кэша

### Мониторинг
Настроено логирование операций кэширования:
```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

## Полезные команды Redis
```bash
# Просмотр всех ключей
KEYS cache:*

# Получение значения
GET cache:products:1

# Очистка всех данных
FLUSHALL
```

## Дополнительная информация
- Проект использует аннотацию `@EnableCaching` для активации механизма кэширования
- Реализована обработка null значений
- Настроена сериализация/десериализация через JSON

## Тестовое покрытие
- Интеграционные тесты с TestContainers
- Тесты REST API
- Тесты кэширования

<br />

-------------------

# Redis Best Practices

## 1. Именование ключей
```redis
# Использовать разделители для структурирования
user:1000:profile
product:123:details

# Использовать префиксы для разных типов данных
cache:products:1
session:user:12345
```
- Использовать понятные и консистентные названия
- Включать тип данных в имя ключа
- Использовать разделители (обычно ":")
- Избегать пробелов и специальных символов

## 2. Управление памятью
- Устанавливать TTL (время жизни) для ключей
```yaml
# Пример из application.yml
spring:
  cache:
    redis:
      time-to-live: 3600000  # 1 час
```
- Использовать maxmemory и политики удаления (eviction policies)
- Мониторить использование памяти
- Регулярно чистить неиспользуемые данные

## 3. Оптимизация производительности
- Использовать пайплайны для множественных операций
- Избегать тяжелых операций (KEYS *)
- Использовать SCAN вместо KEYS
- Правильно выбирать структуры данных:
```redis
# Для простых значений
SET user:1:name "John"

# Для хэш-таблиц
HMSET user:1 name "John" age "30"

# Для списков
LPUSH notifications:user:1 "new_message"
```

## 4. Безопасность
```yaml
spring:
  redis:
    password: your_strong_password
    ssl: true  # если нужно
```
- Всегда использовать аутентификацию
- Ограничивать доступ по IP
- Использовать SSL/TLS для удаленных подключений
- Регулярно обновлять Redis

## 5. Кэширование
```java
// Правильное использование @Cacheable
@Cacheable(
    value = "products",
    key = "#id",
    unless = "#result == null",
    condition = "#id > 0"
)
public Product getProduct(Long id) {
    // ...
}
```
- Выбирать правильное время жизни кэша
- Использовать условия кэширования
- Правильно инвалидировать кэш
- Использовать префиксы для разных окружений

## 6. Мониторинг и логирование
```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```
- Мониторить метрики Redis
- Настроить алерты на критические ситуации
- Логировать важные операции
- Отслеживать паттерны использования

## 7. Отказоустойчивость
```java
@Bean
public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setEnableTransactionSupport(true);
    // ...
    return template;
}
```
- Использовать Redis Sentinel или Redis Cluster для высокой доступности
- Настроить репликацию
- Иметь стратегию бэкапа
- Правильно обрабатывать ошибки подключения

## 8. Тестирование
```java
@Container
private static final GenericContainer<?> redis =
    new GenericContainer<>(DockerImageName.parse("redis:latest"))
        .withExposedPorts(6379);
```
- Использовать TestContainers для интеграционных тестов
- Тестировать сценарии отказа Redis
- Проверять производительность
- Тестировать инвалидацию кэша

## 9. Структуры данных
Выбирать подходящие типы данных Redis:
```redis
# Строки для простых значений
SET key value

# Хэши для объектов
HSET user:1 name "John" age "30"

# Списки для очередей
LPUSH/RPUSH

# Множества для уникальных значений
SADD

# Сортированные множества для рейтингов
ZADD
```

## 10. Масштабирование
- Правильно шардировать данные
- Использовать Redis Cluster для больших наборов данных
- Планировать рост данных
- Мониторить нагрузку

В проекте уже реализованы некоторые из этих практик:
- Правильное именование ключей с префиксами
- Настроенное время жизни кэша
- Использование TestContainers для тестов
- Правильная конфигурация сериализации

<br />

----------------------

## SimpleKeyGenerator

SimpleKeyGenerator - это стандартный генератор ключей в Spring Cache, который используется по умолчанию, если не указан key или свой keyGenerator.
Примеры работы SimpleKeyGenerator
```java
@Service
public class ProductService {

    // Пример 1: Метод без параметров
    @Cacheable("products")
    public List<Product> getAllProducts() {
        return repository.findAll();
    }
    // В Redis будет:
    // Key: "products::SimpleKey[]"
    // (пустой SimpleKey, потому что нет параметров)

    // Пример 2: Один параметр
    @Cacheable("products")
    public Product getProduct(Long id) {
        return repository.findById(id);
    }
    // При вызове getProduct(123L)
    // В Redis будет:
    // Key: "products::123"
    // (просто значение параметра, потому что параметр один)

    // Пример 3: Два примитивных параметра
    @Cacheable("products")
    public List<Product> getProductsByPriceRange(Double min, Double max) {
        return repository.findByPriceBetween(min, max);
    }
    // При вызове getProductsByPriceRange(10.0, 20.0)
    // В Redis будет:
    // Key: "products::SimpleKey[10.0,20.0]"

    // Пример 4: Строковый и числовой параметр
    @Cacheable("products")
    public List<Product> getProductsByCategoryAndPrice(String category, Double price) {
        return repository.findByCategoryAndPrice(category, price);
    }
    // При вызове getProductsByCategoryAndPrice("electronics", 999.99)
    // В Redis будет:
    // Key: "products::SimpleKey[electronics,999.99]"

    // Пример 5: Объект как параметр
    @Cacheable("products")
    public List<Product> getProductsByFilter(ProductFilter filter) {
        return repository.findByFilter(filter);
    }
    // При вызове с filter = new ProductFilter("electronics", 1000)
    // В Redis будет:
    // Key: "products::com.example.ProductFilter@a1b2c3"
    // (хэш-код объекта в конце)

    // Пример 6: Массив как параметр
    @Cacheable("products")
    public List<Product> getProductsByIds(Long[] ids) {
        return repository.findAllById(Arrays.asList(ids));
    }
    // При вызове getProductsByIds([1L, 2L, 3L])
    // В Redis будет:
    // Key: "products::SimpleKey[[1,2,3]]"

    // Пример 7: Список как параметр
    @Cacheable("products")
    public List<Product> getProductsByCategories(List<String> categories) {
        return repository.findByCategories(categories);
    }
    // При вызове getProductsByCategories(["electronics", "gadgets"])
    // В Redis будет:
    // Key: "products::SimpleKey[[electronics,gadgets]]"

    // Пример 8: Несколько разных типов параметров
    @Cacheable("products")
    public List<Product> searchProducts(String query, Double maxPrice, Boolean inStock) {
        return repository.search(query, maxPrice, inStock);
    }
    // При вызове searchProducts("laptop", 1000.0, true)
    // В Redis будет:
    // Key: "products::SimpleKey[laptop,1000.0,true]"
}
```
Важные моменты:
- Для одного параметра - значение используется напрямую
- Для нескольких параметров - создается SimpleKey с массивом значений
- Для объектов - используется toString() или hashCode
- Всегда добавляется префикс из значения value в @Cacheable
- Используется двойное двоеточие (::) как разделитель

<br />


--------------------------------------------
# Self в Spring Cache

## Проблема и решение
Self используется для корректной работы с прокси и кэшированием в Spring.

## Пример использования

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductService self;  // Инъекция самого себя
    
    @Cacheable("products")
    public Product getProduct(Long id) {
        return findProduct(id);
    }
    
    private Product findProduct(Long id) {
        // Реальный поиск продукта
        return new Product(id);
    }
    
    public Product getSomeProduct(Long id) {
        // Вызов через self для работы кэширования
        return self.getProduct(id);
    }
}
```

## Почему это нужно

### 1. Проблема прокси
- Spring создает прокси для классов с аннотациями кэширования
- Когда вы вызываете метод внутри того же класса, вы bypass'ите это прокси
- Кэширование не работает при внутренних вызовах

### 2. Пример проблемы без self
```java
@Service
public class ProductService {
    
    @Cacheable("products")
    public Product getProduct(Long id) {
        return findProduct(id);
    }
    
    public Product getSomeProduct(Long id) {
        // Кэширование НЕ СРАБОТАЕТ, потому что вызов происходит внутри класса
        return this.getProduct(id);
    }
}
```

### 3. Решение с self
```java
@Service
public class ProductService {
    
    @Autowired
    private ProductService self;
    
    @Cacheable("products")
    public Product getProduct(Long id) {
        return findProduct(id);
    }
    
    public Product getSomeProduct(Long id) {
        // Кэширование СРАБОТАЕТ, потому что вызов идет через прокси
        return self.getProduct(id);
    }
}
```

### 4. Альтернативное решение через AopContext
```java
@Service
public class ProductService {
    
    @Cacheable("products")
    public Product getProduct(Long id) {
        return findProduct(id);
    }
    
    public Product getSomeProduct(Long id) {
        // Тоже работает, но менее удобно
        return ((ProductService) AopContext.currentProxy()).getProduct(id);
    }
}
```

## Преимущества использования self
- Более чистый код
- Явное указание на использование прокси
- Работает со всеми аспектами Spring (не только кэширование)
- Легче тестировать

## Недостатки
- Может быть неочевидным для новых разработчиков
- Создает циклическую зависимость (хотя Spring с этим справляется)

## Практический пример использования
```java
@Service
@Slf4j
public class ProductService {
    
    @Autowired
    private ProductService self;
    
    private final Map<Long, Product> productDatabase = new ConcurrentHashMap<>();
    
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        log.info("Fetching product with id: {}", id);
        return productDatabase.get(id);
    }
    
    public List<Product> getMultipleProducts(List<Long> ids) {
        // Используем self для корректной работы кэширования
        return ids.stream()
                 .map(id -> self.getProduct(id))
                 .collect(Collectors.toList());
    }
}
```