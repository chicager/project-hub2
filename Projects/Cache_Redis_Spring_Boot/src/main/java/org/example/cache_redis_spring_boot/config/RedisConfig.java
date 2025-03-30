package org.example.cache_redis_spring_boot.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /*
    RedisTemplate - это главный инструмент для работы с Redis
    <String, Object> - ключи будут строками, значения могут быть любыми объектами
    connectionFactory - Spring автоматически передаст фабрику подключений к Redis
    */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>(); //Создаем новый шаблон
        template.setConnectionFactory(connectionFactory); //Говорим ему, как подключаться к Redis

        // Используем StringRedisSerializer для ключей (Указываем, как преобразовывать ключи в строки для Redis, Это нужно и для обычных ключей, и для ключей в хэш-таблицах Redis)
        template.setKeySerializer(new StringRedisSerializer()); //StringRedisSerializer - самый простой и надежный способ
        template.setHashKeySerializer(new StringRedisSerializer()); //StringRedisSerializer - самый простой и надежный способ

        // Используем GenericJackson2JsonRedisSerializer для значений (Указываем, как преобразовывать значения в JSON для Redis, Это нужно и для обычных значений, и для значений в хэш-таблицах)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(); //GenericJackson2JsonRedisSerializer превращает Java-объекты в JSON и обратно
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet(); //  Проверяем и инициализируем всё настроенное выше (т. е. проверяет, что мы не забыли установить главные свойства)
        return template;
    }

    //Создаем менеджер кэша. Он будет управлять всем кэшированием через аннотации (@Cacheable и др.)
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) //Настраиваем сериализацию ключей для кэш-менеджера, Используем тот же StringRedisSerializer
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())) //Настраиваем сериализацию значений для кэш-менеджера, Используем тот же JSON сериализатор
                .disableCachingNullValues(); // Не кэшируем null значения

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
