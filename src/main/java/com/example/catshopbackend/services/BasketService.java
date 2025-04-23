package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import com.example.catshopbackend.models.Coupon;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class BasketService {
    public ArrayList<ProductDTO> basket = new ArrayList<>();
    private final PrintingServiceTest printingService;

    // Coupon management
    private final Map<String, Coupon> coupons = new HashMap<>();
    private Coupon appliedCoupon;

    // Constructor for initializing available coupons
    public BasketService(PrintingServiceTest printingService) {
        this.printingService = printingService;
        // Add coupon with code 555 and value 5€
        coupons.put("555", new Coupon("555", 5.0, false));
        // Add coupon with code 999 and value 5€
        coupons.put("999", new Coupon("999", 5.0, false));
    }

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
     * Calculates the total amount of the basket without coupon
     * @return Total amount
     */
    public double calculateBasketTotal() {
        return basket.stream()
                .mapToDouble(ProductDTO::getPrice)
                .sum();
    }

    /**
     * Applies a coupon code to the current basket
     * @param couponCode The entered coupon code
     * @return Basket with applied coupon
     */
    public ArrayList<ProductDTO> applyCoupon(int couponCode) {
        String couponCodeStr = String.valueOf(couponCode);

        // Check if a coupon has already been applied
        if (appliedCoupon != null) {
            throw new IllegalStateException("Es wurde bereits ein Gutschein eingelöst");
        }

        Coupon coupon = coupons.get(couponCodeStr);
        if (coupon == null) {
            throw new IllegalArgumentException("Ungültiger Gutscheincode");
        }

        // Special handling for used coupons
        if (coupon.isUsed()) {
            throw new IllegalStateException("Dieser Gutschein wurde bereits eingelöst");
        }

        double basketTotal = calculateBasketTotal();
        if (basketTotal < coupon.getValue()) {
            throw new IllegalStateException("Der Einkaufswert beträgt nur " +
                    String.format("%.2f", basketTotal) +
                    " EUR. Der Gutschein kann nicht eingelöst werden, da der Mindestwert " +
                    String.format("%.2f", coupon.getValue()) + " EUR beträgt.");
        }

        // Apply coupon
        appliedCoupon = coupon;
        coupon.setUsed(true);

        // Add coupon as special product with negative price to the basket
        ProductDTO couponProduct = new ProductDTO();
        couponProduct.setId(-couponCode); // Negative ID to distinguish from normal products
        couponProduct.setName("Gutschein");
        couponProduct.setPrice(-coupon.getValue());
        basket.add(couponProduct);

        return basket;
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
        appliedCoupon = null; // Reset coupon

        return true;
    }
}