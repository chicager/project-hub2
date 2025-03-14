package org.example.cache_caffeine_spring_boot.controller;

import lombok.RequiredArgsConstructor;
import org.example.cache_caffeine_spring_boot.model.Product;
import org.example.cache_caffeine_spring_boot.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)  // если продукт найден, возвращаем 200 OK
                .orElse(ResponseEntity.notFound().build());  // если не найден, возвращаем 404
    }

    @GetMapping("/price/{minPrice}")
    public ResponseEntity<List<Product>> getProductsByMinPrice(@PathVariable Double minPrice) {
        return ResponseEntity.ok(productService.getProductsByMinPrice(minPrice));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Product> findByNameAndPrice(
            @RequestParam String name,
            @RequestParam Double price) {
        return productService.findByNameAndPrice(name, price)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cache/clear-all")
    public ResponseEntity<Void> clearAllCaches() {
        productService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/{cacheName}/clear")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        productService.clearCache(cacheName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/names")
    public ResponseEntity<List<String>> getCacheNames() {
        return ResponseEntity.ok(productService.getCacheNames());
    }

    @DeleteMapping("/cache/{cacheName}/{key}")
    public ResponseEntity<Void> evictFromCache(
            @PathVariable String cacheName,
            @PathVariable String key) {
        productService.evictFromCache(cacheName, key);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/{cacheName}/contents")
    public ResponseEntity<Map<Object, Object>> getCacheContents(@PathVariable String cacheName) {
        return ResponseEntity.ok(productService.getCacheContents(cacheName));
    }

    @GetMapping("/cache/{cacheName}/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats(@PathVariable String cacheName) {
        return ResponseEntity.ok(productService.getCacheStats(cacheName));
    }

    /*
    @ExceptionHandler(Exception.class) - говорит Spring:
      "Этот метод будет обрабатывать все исключения типа Exception"
      "Используй его, когда в контроллере произойдет ошибка"
    Когда происходит ошибка:
      Spring перехватывает исключение
      Вызывает этот метод
      Возвращает клиенту статус 500 (INTERNAL_SERVER_ERROR)
      В теле ответа отправляет сообщение об ошибке
    Например:
      Если в getProduct произойдет ошибка
      Клиент получит ответ:
        {
          "status": 500,
          "body": "Произошла ошибка: [текст ошибки]"
        }
    Это как:
      Обычный код - это как работа без страховки
      Exception Handler - это как система безопасности, которая:
        Ловит ошибки
        Превращает их в понятные сообщения
        Сохраняет приложение от падения
    Однако, в реальном приложении лучше:
      Создать отдельный класс для обработки ошибок
      Обрабатывать разные типы исключений по-разному
      Не показывать клиенту детали ошибок
      Логировать ошибки для отладки
    Spring MVC по умолчанию имеет встроенные обработчики ошибок
    Если бы не было нашего хендлера и произойдет ошибка:
      Приложение продолжит работать
      Клиент получит ответ с ошибкой
      В логах будет информация об ошибке
    Наш @ExceptionHandler нужен для:
      Кастомизации обработки ошибок
      Возврата специфичных для нашего приложения сообщений об ошибках
      Логирования ошибок в нужном формате
    Например, без нашего обработчика клиент получит стандартный ответ Spring:
      {
        "timestamp": "2024-03-10T12:34:56.789",
        "status": 500,
        "error": "Internal Server Error",
        "path": "/api/products/1"
      }
    Поэтому:
      Приложение не упадет в любом случае
      Наш обработчик нужен только для кастомизации ответа
      Без него Spring сам обработает ошибку
    */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Произошла ошибка: " + e.getMessage());
    }
}
