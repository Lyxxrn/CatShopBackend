package com.example.catshopbackend.services.payment;

public interface PaymentStrategy {
    /**
     * Process the payment with the given amount
     * @param amount the amount to be paid
     * @return true if payment was successful, false otherwise
     */
    boolean pay(double amount);

    /**
     * Get the name of the payment method
     * @return payment method name
     */
    String getMethodName();
}