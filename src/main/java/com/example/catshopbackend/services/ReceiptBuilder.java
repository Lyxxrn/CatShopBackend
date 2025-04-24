package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiptBuilder {
    private StringBuilder receipt = new StringBuilder();
    private StringBuilder stornos = new StringBuilder();
    private double totalAmount = 0.0;
    private int totalItems = 0;
    private String cashierName;
    private String paymentMethod;
    private ArrayList<ProductDTO> products;

    private final String seperatorStrong = "============================";
    private final String seperatorStrongTitle = "================";
    private final String seperatorWeak = "----------------------------";
    private final String[] titleParts = {
            "HealthyCat Shop",
            "Dein Lieblingsladen für",
            "Katzen"
    };
    private final String[] catImgParts = {
            " /\\_/\\ ",
            "(=^.^=)",
            "(\\\")_(\\\")",
            " /   \\ ",
            "(     )",
            "(___(__)"
    };

    public ReceiptBuilder withCashier(String cashierName) {
        this.cashierName = cashierName;
        return this;
    }

    public ReceiptBuilder withPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public ReceiptBuilder withProducts(ArrayList<ProductDTO> products) {
        this.products = products;
        processProducts();
        return this;
    }

    private void processProducts() {
        Map<String, ProductCount> productMap = new HashMap<>();

        for (ProductDTO product : products) {
            String key = product.getId() + "_" + product.getPrice();
            productMap.putIfAbsent(key, new ProductCount(product, 0));

            ProductCount count = productMap.get(key);
            count.incrementQuantity();
        }

        for (ProductCount productCount : productMap.values()) {
            addProductToReceipt(productCount.getProduct(), productCount.getQuantity());
        }
    }

    private void addProductToReceipt(ProductDTO product, int quantity) {
        String name = product.getName();
        double price = product.getPrice();
        StringBuilder target = price < 0 ? stornos : receipt;

        double total = price * quantity;
        totalAmount += total;

        if (price > 0) {
            totalItems += quantity;
        }

        String formattedName = name.length() > 7 ? name.substring(0, 6) + "." : name;
        String formattedPrice = price < 0 ?
                String.format("%.2f EUR", price) :
                String.format("%.2f EUR", price);
        String formattedTotal = price < 0 ?
                String.format("%.2f EUR", total) :
                String.format("%.2f EUR", total);

        target.append(String.format("%-8s %6s %3d %7s",
                        formattedName, formattedPrice, quantity, formattedTotal))
                .append("\n");
    }

    public ReceiptData build() {
        return new ReceiptData(
                receipt.toString(),
                stornos.toString(),
                totalAmount,
                totalItems,
                cashierName,
                paymentMethod,
                seperatorStrong,
                seperatorStrongTitle,
                seperatorWeak,
                titleParts,
                catImgParts
        );
    }

    @Getter
    private static class ProductCount {
        private ProductDTO product;
        private int quantity;

        public ProductCount(ProductDTO product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public void incrementQuantity() {
            this.quantity++;
        }
    }

    @Getter
    public static class ReceiptData {
        private final String receiptContent;
        private final String stornosContent;
        private final double totalAmount;
        private final int totalItems;
        private final String cashierName;
        private final String paymentMethod;
        private final String seperatorStrong;
        private final String seperatorStrongTitle;
        private final String seperatorWeak;
        private final String[] titleParts;
        private final String[] catImgParts;

        public ReceiptData(String receiptContent, String stornosContent, double totalAmount,
                           int totalItems, String cashierName, String paymentMethod,
                           String seperatorStrong, String seperatorStrongTitle,
                           String seperatorWeak, String[] titleParts, String[] catImgParts) {
            this.receiptContent = receiptContent;
            this.stornosContent = stornosContent;
            this.totalAmount = totalAmount;
            this.totalItems = totalItems;
            this.cashierName = cashierName;
            this.paymentMethod = paymentMethod;
            this.seperatorStrong = seperatorStrong;
            this.seperatorStrongTitle = seperatorStrongTitle;
            this.seperatorWeak = seperatorWeak;
            this.titleParts = titleParts;
            this.catImgParts = catImgParts;
        }
    }
}