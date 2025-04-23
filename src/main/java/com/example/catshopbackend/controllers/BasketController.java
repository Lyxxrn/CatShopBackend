package com.example.catshopbackend.controllers;

import com.example.catshopbackend.dto.ProductDTO;
import com.example.catshopbackend.services.BasketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/basket")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    @GetMapping
    public ResponseEntity<ArrayList<ProductDTO>> getBasket() {
        return ResponseEntity.ok(basketService.basket);
    }

    @PostMapping("/add")
    public ResponseEntity<ArrayList<ProductDTO>> addToBasket(@RequestBody Object input) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            if (input instanceof List) {
                List<?> inputList = mapper.convertValue(input, List.class);
                for (Object item : inputList) {
                    ProductDTO product = mapper.convertValue(item, ProductDTO.class);
                    basketService.addToBasket(product);
                }
                return ResponseEntity.ok(basketService.basket);
            } else {
                ProductDTO product = mapper.convertValue(input, ProductDTO.class);
                return ResponseEntity.ok(basketService.addToBasket(product));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input format. Expected ProductDTO or array of ProductDTO: " + e.getMessage());
        }
    }

    @PostMapping("/removeLast")
    public ResponseEntity<ArrayList<ProductDTO>> removeLastFromBasket() {
        return ResponseEntity.ok(basketService.removeLastFromBasket());
    }

    @PostMapping("/removeSpecific")
    public ResponseEntity<ArrayList<ProductDTO>> removeSpecificFromBasket(@RequestBody ProductDTO product) {
        System.out.println("Removing product: " + product);
        return ResponseEntity.ok(basketService.removeSpecificFromBasket(product));
    }

    @PostMapping("/applyCoupon/{couponCode}")
    public ResponseEntity<?> applyCoupon(@PathVariable int couponCode) {
        try {
            return ResponseEntity.ok(basketService.applyCoupon(couponCode));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Dieser Gutschein wurde bereits eingelöst")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<Boolean> completeBasket() {
        return ResponseEntity.ok(basketService.completeBasket());
    }
}