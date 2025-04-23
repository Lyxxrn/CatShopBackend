package com.example.catshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private boolean inStock;
    private double rating;
    private List<TagDTO> tags;
}