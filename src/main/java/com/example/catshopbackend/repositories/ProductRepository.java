package com.example.catshopbackend.repositories;

import com.example.catshopbackend.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, Integer> {

    List<Product> findByCategory(String category);
}
