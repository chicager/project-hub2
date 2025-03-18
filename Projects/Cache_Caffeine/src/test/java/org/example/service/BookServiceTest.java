package org.example.service;

import org.example.model.Book;
import org.example.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookService bookService;

    @BeforeEach
    void setUp() {
        BookRepository bookRepository = new BookRepository();
        bookService = new BookService(bookRepository);
    }

    @Test
    void testGetBookWithSimpleCache() {
        // Act
        Book firstCall = bookService.getBookWithSimpleCache(1L);
        Book secondCall = bookService.getBookWithSimpleCache(1L);

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void testGetBookWithExpiringCache() {
        // Act
        Book firstCall = bookService.getBookWithExpiringCache(1L);
        Book secondCall = bookService.getBookWithExpiringCache(1L);

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void testGetBookWithLoadingCache() {
        // Act
        Book firstCall = bookService.getBookWithLoadingCache(1L);
        Book secondCall = bookService.getBookWithLoadingCache(1L);

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void testCachePerformance() {
        // Act
        long startTime = System.currentTimeMillis();
        bookService.getBookWithSimpleCache(1L);
        long firstCallTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        bookService.getBookWithSimpleCache(1L);
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Assert
        assertTrue(secondCallTime < firstCallTime, "Второй вызов должен быть быстрее первого благодаря кэшу");
    }

    @Test
    void testInvalidateCache() {
        // Arrange
        Book firstCall = bookService.getBookWithSimpleCache(1L);

        // Act
        bookService.invalidateCache(1L);
        Book secondCall = bookService.getBookWithSimpleCache(1L);

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall); // Должны быть равны, так как данные те же
    }

    @Test
    void testNonExistentBook() {
        // Act
        Book book = bookService.getBookWithSimpleCache(999L);

        // Assert
        assertNull(book);
    }
}