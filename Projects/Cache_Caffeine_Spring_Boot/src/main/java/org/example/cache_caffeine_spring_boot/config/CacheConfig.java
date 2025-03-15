package org.example.cache_caffeine_spring_boot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /*
    В данном случае мы создаем CacheManager потому что мы используем конкретную реализацию кеша - Caffeine.
    По умолчанию Spring Boot не создает CacheManager, потому что:
      Spring поддерживает разные реализации кеша (Caffeine, EhCache, Guava и др.)
      Spring не знает, какую именно реализацию мы хотим использовать
      Нам нужно самим указать, как именно мы хотим кешировать данные
    Мы:
      Создаем CaffeineCacheManager - это конкретная реализация кеша
      Настраиваем его с помощью конфигурации Caffeine
      Возвращаем его как CacheManager
    Это как:
      Spring знает, что нам нужен автомобиль (CacheManager)
      Но не знает, какой именно (Caffeine, EhCache и т.д.)
      Мы сами говорим: "Нам нужен автомобиль марки Caffeine с такими-то характеристиками"
    Если бы мы не создали CacheManager:
      Spring не знал бы, какую реализацию кеша использовать
      Кеширование не работало бы
      Получили бы ошибку при запуске
    Поэтому создание CacheManager - это обязательный шаг при использовании Caffeine в Spring Boot.
    */

    /*
    Создавать CacheManager вручную не обязательно, если вас устраивают настройки по умолчанию
    или если Spring Boot может автоматически определить используемую библиотеку кэширования.
    Для кастомизации или специфичных сценариев можно определить свой CacheManager.

    При использовании spring-boot-starter-cache в Spring Boot вам не обязательно создавать CacheManager вручную.
    Spring Boot предоставляет автоматическую настройку (auto-configuration) для кэширования,
    которая создает CacheManager на основе доступных зависимостей в classpath.
    Как это работает:
      Автоматическая конфигурация:
        Spring Boot проверяет наличие библиотек кэширования (например, Caffeine, EhCache, Redis, Hazelcast) в classpath.
        Если найдена одна из поддерживаемых библиотек, Spring Boot автоматически создает соответствующий CacheManager.
        Если никакой библиотеки кэширования не обнаружено, Spring Boot использует простой кэш на основе ConcurrentHashMap.
        Если вы добавите, например, caffeine в зависимости, то Spring Boot автоматически создаст CaffeineCacheManager.
    Кастомизация:
      Если вы хотите настроить параметры кэша (например, TTL, размер), вы можете:
        Использовать свойства в application.properties (зависит от реализации, например, для Caffeine или Redis).
        Создать свой CacheManager bean в конфигурации, чтобы переопределить настройки по умолчанию.
    Когда нужно создавать CacheManager явно:
      Если вы используете несколько реализаций кэша и хотите выбрать одну.
      Если вам нужна нестандартная конфигурация, которую нельзя задать через свойства.
      Если вы хотите использовать "no-op" кэш (отключение кэширования в тестах).
    */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                //кастомизируем каффеин
                Caffeine.newBuilder()
                    .expireAfterWrite(60, TimeUnit.MINUTES)
                    .initialCapacity(100)
                    .maximumSize(500)
                    .recordStats()
        );
        return cacheManager;
    }
}
