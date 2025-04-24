package com.example.catshopbackend.factory;

import com.example.catshopbackend.dto.ProductDTO;

public class ProductFactory {

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