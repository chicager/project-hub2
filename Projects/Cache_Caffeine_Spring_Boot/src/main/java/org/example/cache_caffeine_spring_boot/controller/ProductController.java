package org.example.cache_caffeine_spring_boot.controller;

import lombok.RequiredArgsConstructor;
import org.example.cache_caffeine_spring_boot.service.ProductService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // TODO: 14.03.2025 add 
}
