package org.example.repository;

import org.example.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.example.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class BookRepositoryTest {

    private BookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new BookRepository();
    }

    @Test
    void testFindById() {
        // Act
        Book book = repository.findByid(BOOK_ID);

        // Assert
        assertNotNull(book);
        assertEquals(BOOK_ID, book.getId());
        assertEquals(BOOK_TITLE, book.getTitle());
        assertEquals(BOOK_AUTHOR, book.getAuthor());
        assertEquals(BOOK_PRICE, book.getPrice());
    }

    @Test
    void testFindByIdNonExistent() {
        // Act
        Book book = repository.findByid(NON_EXISTENT_BOOK_ID);

        // Assert
        assertNull(book);
    }

    @Test
    void testFindByIdPerformance() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        repository.findByid(BOOK_ID);
        long endTime = System.currentTimeMillis();

        // Assert
        assertTrue(endTime - startTime >= 1000, "Операция должна занимать не менее 1 секунды");
    }
}