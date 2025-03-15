package org.example.cache_caffeine_spring_boot.service;

import org.example.cache_caffeine_spring_boot.model.Product;
import org.example.cache_caffeine_spring_boot.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(100.0);
        testProduct.setStock(10);
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct.getName(), result.get().getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductsByMinPrice_ShouldReturnFilteredList() {
        // Arrange
        Product product2 = new Product();
        product2.setPrice(200.0);
        List<Product> allProducts = Arrays.asList(testProduct, product2);
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        List<Product> result = productService.getProductsByMinPrice(150.0);

        // Assert
        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getPrice());
        verify(productRepository).findAll();
    }

    @Test
    void createProduct_ShouldSaveProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProduct_ShouldUpdateProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).save(testProduct);
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        // Arrange
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).deleteById(1L);
    }

    @Test
    void findByNameAndPrice_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        // Act
        Optional<Product> result = productService.findByNameAndPrice("Test Product", 100.0);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct.getName(), result.get().getName());
        assertEquals(testProduct.getPrice(), result.get().getPrice());
        verify(productRepository).findAll();
    }
}