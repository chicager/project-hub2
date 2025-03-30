package org.example.cache_redis_spring_boot.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/*
KeyGenerator - это интерфейс Spring, который говорит "я умею создавать ключи для кэша"
Реализуя этот интерфейс, мы создаем свой способ генерации ключей
Spring будет использовать наш генератор, когда мы укажем его в аннотации @Cacheable(keyGenerator = "cacheKeyGenerator")

"productCacheKeyGenerator" - это имя бина
Имя нужно, потому что у нас два генератора ключей, и мы должны различать их
Это имя мы используем в аннотации @Cacheable(keyGenerator = "productCacheKeyGenerator")
Без имени Spring не знал бы, какой именно генератор использовать:
  @Component  // Так нельзя - Spring запутается между двумя KeyGenerator
  public class ProductCacheKeyGenerator implements KeyGenerator { ... }

  @Component  // Так нельзя - Spring запутается между двумя KeyGenerator
  public class CacheKeyGenerator implements KeyGenerator { ... }
*/
@Component("productCacheKeyGenerator")
public class ProductCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("product:");  // Префикс для всех ключей

        // Генерируем ключ на основе метода
        if (method.getName().startsWith("get")) {
            // Для методов получения продукта
            if (params != null && params.length > 0) {
                sb.append(params[0]); // ID продукта
            }
        } else if (method.getName().startsWith("getAll")) {
            // Для методов получения списка продуктов
            if (params != null && params.length >= 2) {
                sb.append("page:").append(params[0]);
                sb.append(":size:").append(params[1]);
            }
        } else if (method.getName().startsWith("getHot")) {
            // Для горячих продуктов добавляем временную метку
            sb.append("hot:").append(System.currentTimeMillis() / 3600000); // Ключ меняется каждый час
        }

        return sb.toString();
    }
}
