package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import com.example.catshopbackend.mapper.ProductMapper;
import com.example.catshopbackend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductDTO> getAllProducts() {
        return productMapper.toDTOList(productRepository.findAll());
    }

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDTO);
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        return productMapper.toDTOList(productRepository.findByCategory(category));
    }
}