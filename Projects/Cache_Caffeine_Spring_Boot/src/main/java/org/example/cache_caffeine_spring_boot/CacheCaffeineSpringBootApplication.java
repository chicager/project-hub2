package org.example.cache_caffeine_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


/*
Аннотация @EnableCaching включает механизм кеширования в Spring Boot приложении.
Без этой аннотации:
  Spring не будет обрабатывать аннотации @Cacheable, @CachePut, @CacheEvict
  Кеширование не будет работать
  Все методы будут выполняться каждый раз, даже если мы указали @Cacheable

Так же эту аннотацию можно добавлять в класс конфигурации (в нашем случае в класс CacheConfig), а не здесь
*/
@EnableCaching
@SpringBootApplication
public class CacheCaffeineSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheCaffeineSpringBootApplication.class, args);
	}

}
