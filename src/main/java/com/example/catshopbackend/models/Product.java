package com.example.catshopbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Products")
public class Product {

    @Id
    private int id; // resembles the product barcode

    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private boolean inStock;
    private double rating;
    private List<Tag> tags;
    private int quantity;
}