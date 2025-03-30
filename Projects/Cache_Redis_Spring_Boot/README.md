# Пример работы с кешем Redis в Spring Boot

------------

## Общее инфо

в pom.xml подробно расписаны значения свойств
подробно распсиано про логгирование в application.yml
в CacheKeyGenerator расписано как на прмиере как лучше генерировать ключи
Подробно расписан конфиг Redis в классе RedisConfig

добавить инструменты и best practices в репо для работы с редисом
добавить сам редис (мб еще и anotherRedis)
оформить readme (попросить сгенерить + взять инфу из ворда + своя инфа сверху и снизу)
зачем нам сacheName

тесты
зачем нужно self для работы с кешем и мб сделать это в тестах



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

