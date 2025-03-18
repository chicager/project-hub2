package org.example;

import org.example.model.Book;
import org.example.repository.BookRepository;
import org.example.service.BookService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CachingDemoTest {

    @Test
    void testMainExecution() {
        // Arrange
        BookRepository bookRepository = new BookRepository();
        BookService bookService = new BookService(bookRepository);

        // Act & Assert
        // Проверяем, что сервис возвращает корректные данные
        Book book1 = bookService.getBookWithSimpleCache(1L);
        assertNotNull(book1);
        assertEquals("Война и Мир", book1.getTitle());
        assertEquals("Лев Толстой", book1.getAuthor());

        // Проверяем, что кэш работает (второй запрос должен быть быстрее)
        long startTime = System.currentTimeMillis();
        Book book2 = bookService.getBookWithSimpleCache(1L);
        long endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime < 1000, "Второй запрос должен быть из кэша и быстрее");

        // Проверяем работу автозагружающего кэша
        Book book3 = bookService.getBookWithLoadingCache(2L);
        assertNotNull(book3);
        assertEquals("Преступление и наказание", book3.getTitle());
        assertEquals("Федор Достоевский", book3.getAuthor());

        // Проверяем инвалидацию кэша
        bookService.invalidateCache(1L);
        startTime = System.currentTimeMillis();
        Book book4 = bookService.getBookWithSimpleCache(1L);
        endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime >= 1000, "После инвалидации кэша запрос должен быть медленным");
    }
}