package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.example.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(BOOK_ID, BOOK_TITLE, BOOK_AUTHOR, BOOK_PRICE);
    }

    @Test
    void testBookCreation() {
        // Assert
        assertEquals(BOOK_ID, book.getId());
        assertEquals(BOOK_TITLE, book.getTitle());
        assertEquals(BOOK_AUTHOR, book.getAuthor());
        assertEquals(BOOK_PRICE, book.getPrice());
    }

    @Test
    void testBookSetters() {
        // Act
        book.setId(2L);
        book.setTitle("Анна Каренина");
        book.setAuthor("Лев Толстой");
        book.setPrice(new BigDecimal("24.99"));

        // Assert
        assertEquals(2L, book.getId());
        assertEquals("Анна Каренина", book.getTitle());
        assertEquals("Лев Толстой", book.getAuthor());
        assertEquals(new BigDecimal("24.99"), book.getPrice());
    }

    @Test
    void testBookToString() {
        // Act
        String result = book.toString();

        // Assert
        assertTrue(result.contains("id=" + BOOK_ID));
        assertTrue(result.contains("title='" + BOOK_TITLE + "'"));
        assertTrue(result.contains("author='" + BOOK_AUTHOR + "'"));
        assertTrue(result.contains("price=" + BOOK_PRICE));
    }
}
