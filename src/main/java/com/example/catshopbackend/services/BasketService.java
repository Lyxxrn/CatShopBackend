package com.example.catshopbackend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.catshopbackend.dto.ProductDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasketService {
    public ArrayList<ProductDTO> basket = new ArrayList<>();
    private final PrintingServiceTest printingService;

    private ProductDTO nullifyTransaction(ProductDTO productToBeNullified) {
        productToBeNullified.setPrice(productToBeNullified.getPrice()*-1);
        return productToBeNullified;
    }

    public ArrayList<ProductDTO> addToBasket(ProductDTO product) {
        basket.add(product);
        return basket;
    }

    public ArrayList<ProductDTO> removeLastFromBasket() {
        if (!basket.isEmpty()) {
            basket.add(nullifyTransaction(basket.getLast()));
        }
        return basket;
    }

    public ArrayList<ProductDTO> removeSpecificFromBasket(ProductDTO product) {
        basket.add(nullifyTransaction(product));
        System.out.println(basket);
        return basket;
    }

    /**
     * Calculates the total amount of the basket
     * @return Total amount
     */
    public double calculateBasketTotal() {
        return basket.stream()
                .mapToDouble(ProductDTO::getPrice)
                .sum();
    }

    public Boolean completeBasket() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String cashierName = authentication.getName();

        ArrayList<ProductDTO> basketCopy = new ArrayList<>(basket);

        // Payment method (could be dynamic later)
        String paymentMethod = "Barzahlung";

        printingService.printReceipt("POS-58", cashierName, paymentMethod, basketCopy);

        System.out.println(basketCopy);
        basket.clear();
        return true;
    }
}