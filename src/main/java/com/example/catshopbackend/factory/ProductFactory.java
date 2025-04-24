package com.example.catshopbackend.factory;

import com.example.catshopbackend.dto.ProductDTO;

public class ProductFactory {

    /**
     * Creates a regular product
     * @param id product ID
     * @param name product name
     * @param price product price
     * @return new ProductDTO instance
     */
    public static ProductDTO createProduct(Integer id, String name, double price) {
        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }

    /**
     * Creates a storno product from an existing product
     * @param originalProduct the product to be reversed
     * @return new ProductDTO instance with negative price
     */
    public static ProductDTO createStornoProduct(ProductDTO originalProduct) {
        ProductDTO stornoProduct = new ProductDTO();
        stornoProduct.setId(originalProduct.getId());
        stornoProduct.setName(originalProduct.getName());
        stornoProduct.setPrice(-Math.abs(originalProduct.getPrice()));
        return stornoProduct;
    }
}