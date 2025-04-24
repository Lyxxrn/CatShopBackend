package com.example.catshopbackend.services.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentContext {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public boolean executePayment(double amount) {
        if (paymentStrategy == null) {
            throw new IllegalStateException("No payment strategy has been set");
        }
        return paymentStrategy.pay(amount);
    }

    public String getPaymentMethodName() {
        if (paymentStrategy == null) {
            throw new IllegalStateException("No payment strategy has been set");
        }
        return paymentStrategy.getMethodName();
    }
}