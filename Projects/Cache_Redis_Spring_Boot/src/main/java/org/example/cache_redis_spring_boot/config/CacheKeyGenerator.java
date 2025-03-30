package org.example.cache_redis_spring_boot.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/*
KeyGenerator - это интерфейс Spring, который говорит "я умею создавать ключи для кэша"
Реализуя этот интерфейс, мы создаем свой способ генерации ключей
Spring будет использовать наш генератор, когда мы укажем его в аннотации @Cacheable(keyGenerator = "cacheKeyGenerator")
*/
@Component
public class CacheKeyGenerator implements KeyGenerator {

    /*
    Пример работы логики нашего ключа:
      Допустим, у нас есть метод в ProductService:
        @Cacheable(keyGenerator = "cacheKeyGenerator")
        public Product getProduct(Long id, String region) {
            // код метода
        }
      Когда мы вызываем:
        productService.getProduct(123L, "EU");
      Генератор создаст ключ:
        ProductService:getProduct:123:EU

    Как это используется:
      // Первый вызов
      productService.getProduct(123L, "EU");
      // Spring:
      // 1. Генерирует ключ "ProductService:getProduct:123:EU"
      // 2. Проверяет есть ли значение в Redis
      // 3. Нет -> выполняет метод и сохраняет результат

      // Второй вызов с теми же параметрами
      productService.getProduct(123L, "EU");
      // Spring:
      // 1. Генерирует тот же ключ "ProductService:getProduct:123:EU"
      // 2. Находит значение в Redis
      // 3. Возвращает закэшированное значение без выполнения метода

    Преимущества такого подхода:
      Ключи содержат информацию о классе и методе
      Легко понять, откуда взялся кэш
      Можно легко найти все ключи определенного метода
      Уникальные ключи даже если параметры одинаковые, но методы разные
    В Redis мы увидим (с учетом префикса из application.yml):
      cache:ProductService:getProduct:123:EU -> {product data}
    */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        // Добавляем имя класса
        sb.append(target.getClass().getSimpleName());
        sb.append(":");
        // Добавляем имя метода
        sb.append(method.getName());

        // Добавляем все параметры
        if (params != null) {
            for (Object param : params) {
                sb.append(":");
                if (param != null) {
                    sb.append(param.toString());
                } else {
                    sb.append("null");
                }
            }
        }

        return sb.toString();
    }
}
