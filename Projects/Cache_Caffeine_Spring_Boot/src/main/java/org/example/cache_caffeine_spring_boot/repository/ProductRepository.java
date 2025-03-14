package org.example.cache_caffeine_spring_boot.repository;

import org.example.cache_caffeine_spring_boot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
