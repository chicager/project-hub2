package org.example.cache_redis_spring_boot.controller;

import lombok.RequiredArgsConstructor;
import org.example.cache_redis_spring_boot.model.Product;
import org.example.cache_redis_spring_boot.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping("/conditional/{id}")
    public Product getProductWithCondition(@PathVariable Long id) {
        return productService.getProductWithCondition(id);
    }

    @GetMapping("/unless/{id}")
    public Product getProductWithUnless(@PathVariable Long id) {
        return productService.getProductWithUnless(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @DeleteMapping("/cache")
    public void clearCache() {
        productService.clearCache();
    }

    @GetMapping("/list")
    public List<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getAllProducts(page, size);
    }

    @GetMapping("/hot/{id}")
    public Product getHotProduct(@PathVariable Long id) {
        return productService.getHotProduct(id);
    }

    // Новые эндпоинты для тестирования CacheKeyGenerator
    @GetMapping("/custom-key/{id}")
    public Product getProductWithCustomKey(
            @PathVariable Long id,
            @RequestParam(defaultValue = "default") String region) {
        return productService.getProductWithCustomKey(id, region);
    }

    @GetMapping("/product-key/{id}")
    public Product getProductWithProductKey(@PathVariable Long id) {
        return productService.getProductWithProductKey(id);
    }

    @GetMapping("/product-key/list")
    public List<Product> getAllProductsWithProductKey(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getAllProductsWithProductKey(page, size);
    }

    // Метод для инициализации тестовых данных
    @PostMapping("/init")
    public void initTestData() {
        Product product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Smartphone")
                .description("Latest smartphone")
                .price(new BigDecimal("1299.99"))
                .stock(15)
                .build();

        Product product3 = Product.builder()
                .id(3L)
                .name("Headphones")
                .description("Wireless headphones")
                .price(new BigDecimal("199.99"))
                .stock(20)
                .build();

        productService.updateProduct(product1);
        productService.updateProduct(product2);
        productService.updateProduct(product3);
    }
}
