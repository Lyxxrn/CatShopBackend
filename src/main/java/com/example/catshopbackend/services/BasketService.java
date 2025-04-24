package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import com.example.catshopbackend.factory.ProductFactory;
import com.example.catshopbackend.services.payment.CashPaymentStrategy;
import com.example.catshopbackend.services.payment.PaymentContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BasketService {
    public ArrayList<ProductDTO> basket = new ArrayList<>();
    private final PrintingService printingService;
    private final PaymentContext paymentContext;

    @PostConstruct
    public void init() {
        paymentContext.setPaymentStrategy(new CashPaymentStrategy());
    }

    private ProductDTO nullifyTransaction(ProductDTO productToBeNullified) {
        return ProductFactory.createStornoProduct(productToBeNullified);
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

        double totalAmount = calculateBasketTotal();
        ArrayList<ProductDTO> basketCopy = new ArrayList<>(basket);

        boolean paymentSuccessful = paymentContext.executePayment(totalAmount);

        if (paymentSuccessful) {
            // Get the payment method name from the strategy
            String paymentMethod = paymentContext.getPaymentMethodName();

            printingService.printReceipt("POS-58", cashierName, paymentMethod, basketCopy);

            System.out.println(basketCopy);
            basket.clear();
            return true;
        } else {
            return false;
        }
    }
}