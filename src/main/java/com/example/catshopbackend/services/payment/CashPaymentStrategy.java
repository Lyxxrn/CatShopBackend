package com.example.catshopbackend.services.payment;

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean pay(double amount) {
        // Simplified implementation for cash payment
        System.out.println("Zahlung in bar: " + amount + " EUR");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Barzahlung";
    }
}