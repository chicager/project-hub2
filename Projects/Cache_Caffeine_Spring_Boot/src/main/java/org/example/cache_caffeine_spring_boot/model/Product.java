package org.example.cache_caffeine_spring_boot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@ToString(exclude = {"relatedEntities"})  // исключаем связанные сущности
@EqualsAndHashCode(of = {"id"})  // используем только id для сравнения
@Entity
public class Product {

    /*
    GenerationType.IDENTITY означает, что:
    Значение id будет генерироваться базой данных
    Каждая база данных делает это по-своему:
      MySQL использует AUTO_INCREMENT
      PostgreSQL использует SERIAL
      H2 использует AUTO_INCREMENT
    Почему мы используем IDENTITY:
      Это самый простой способ генерации id
      База данных сама гарантирует уникальность
      Не требует дополнительных таблиц или последовательностей
      Хорошо работает с большинством баз данных
    Есть другие стратегии:
      GenerationType.SEQUENCE - использует последовательности в базе данных
      GenerationType.TABLE - использует отдельную таблицу для генерации id
      GenerationType.AUTO - JPA сам выбирает стратегию
    В нашем случае IDENTITY подходит потому что:
      Мы используем H2 (in-memory база данных)
      Нам не нужна сложная логика генерации id
      Мы хотим простоту и надежность
    Это как:
      IDENTITY - это как автоматическая нумерация в очереди
      База данных сама следит за порядком номеров
      Нам не нужно самим придумывать номера
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
}
