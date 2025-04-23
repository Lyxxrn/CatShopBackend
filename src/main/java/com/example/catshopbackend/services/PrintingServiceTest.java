package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PrintingServiceTest {

    // helpers to build receipts
    private final String seperatorStrong = "============================";
    private final String seperatorStrongTitle = "================";
    private final String seperatorWeak = "----------------------------";
    private final String[] titleParts = {
            // needs to be manually centered
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

    private static StringBuilder receipt = new StringBuilder();
    private static StringBuilder stornos = new StringBuilder();
    private static double totalAmount = 0.0;
    private static int totalItems = 0;

    /**
     * Adds a product to the receipt
     * @param product the ProductDTO to be added
     * @param quantity quantity
     */
    public static void addProductToReceipt(ProductDTO product, int quantity) {
        String name = product.getName();
        double price = product.getPrice();
        StringBuilder target = price < 0 ? stornos : receipt;

        double total = price * quantity;
        totalAmount += total;

        if (price > 0) {
            totalItems += quantity;
        }

        // Format product name to max. 8 characters
        String formattedName = name.length() > 7 ? name.substring(0, 6) + "." : name;
        String formattedPrice = price < 0 ?
                String.format("%.2f EUR", price) :
                String.format("%.2f EUR", price);
        String formattedTotal = price < 0 ?
                String.format("%.2f EUR", total) :
                String.format("%.2f EUR", total);

        // Format: "Name Price Quantity Total"
        target.append(String.format("%-8s %6s %3d %7s",
                        formattedName, formattedPrice, quantity, formattedTotal))
                .append("\n");
    }

    /**
     * Processes a list of products for the receipt, grouping identical products
     * @param products the list of ProductDTO objects
     */
    public void processProductsForReceipt(ArrayList<ProductDTO> products) {
        // Reset receipt data
        receipt = new StringBuilder();
        stornos = new StringBuilder();
        totalAmount = 0.0;
        totalItems = 0;

        // Group products by ID and price (to separate regular and storno items)
        Map<String, ProductCount> productMap = new HashMap<>();

        for (ProductDTO product : products) {
            // Unique key combining ID and price to separate stornos from regular items
            String key = product.getId() + "_" + product.getPrice();
            productMap.putIfAbsent(key, new ProductCount(product, 0));

            ProductCount count = productMap.get(key);
            count.incrementQuantity();
        }

        // Add each product with its quantity to the receipt
        for (ProductCount productCount : productMap.values()) {
            addProductToReceipt(productCount.getProduct(), productCount.getQuantity());
        }
    }

    /**
     * Prints the receipt with all added products
     * @param printerName name of the printer
     * @param cashierName name of the cashier (SCO1, SCO2, etc.)
     * @param paymentMethod payment method
     * @param products list of products to add to the receipt
     */
    public void printReceipt(String printerName, String cashierName, String paymentMethod, ArrayList<ProductDTO> products) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        System.out.println("Verfügbare Drucker:");
        for (PrintService printer : printServices) {
            System.out.println(" - " + printer.getName());
        }
        // Process products first
        processProductsForReceipt(products);

        PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
        EscPos escpos;
        try {
            // Für Testzwecke: Erzeuge einen String, der den Bon-Inhalt repräsentiert
            StringBuilder consoleBon = new StringBuilder();

            // Header
            consoleBon.append(seperatorStrongTitle).append("\n");
            for (String line : titleParts) {
                consoleBon.append(line).append("\n");
            }
            consoleBon.append(seperatorStrongTitle).append("\n");

            // Cat image
            for (String line : catImgParts) {
                consoleBon.append(line).append("\n");
            }

            // Date and cashier information
            consoleBon.append(seperatorStrong).append("\n")
                    .append("Datum: ").append(java.time.LocalDate.now()).append("\n")
                    .append("Uhrzeit: ").append(java.time.LocalTime.now().withNano(0)).append("\n")
                    .append("Kassierer: ").append(cashierName).append("\n")
                    .append(seperatorStrong).append("\n")
                    .append("Artikel   Preis Menge Total").append("\n")
                    .append(seperatorWeak).append("\n");

            // Product items
            consoleBon.append(receipt.toString());

            // Stornos if any
            if (stornos.length() > 0) {
                consoleBon.append(seperatorWeak).append("\n")
                        .append("STORNIERTE ARTIKEL").append("\n")
                        .append(seperatorWeak).append("\n")
                        .append(stornos.toString());
            }

            // Total
            double mwst = totalAmount * 0.19;
            double totalWithTax = totalAmount + mwst;

            consoleBon.append(seperatorWeak).append("\n")
                    .append(String.format("Gesamt          %2d %7.2f EUR", totalItems, totalAmount)).append("\n")
                    .append(seperatorStrong).append("\n")
                    .append(String.format("MwSt. (19%%)  %.2f EUR", mwst)).append("\n")
                    .append(String.format("Gesamt inkl. MwSt. %.2f EUR", totalWithTax)).append("\n")
                    .append(seperatorStrong).append("\n")
                    .append("Zahlungsmethode: ").append(paymentMethod).append("\n")
                    .append(seperatorStrong).append("\n")
                    .append("Vielen Dank für Ihren Einkauf!").append("\n")
                    .append("Besuchen Sie uns bald wieder im").append("\n")
                    .append("HealthyCat Shop!").append("\n")
                    .append(seperatorStrong).append("\n")
                    .append("HealthyCat Shop").append("\n")
                    .append("Musterstraße 123").append("\n")
                    .append("12345 Musterstadt").append("\n")
                    .append("Tel: 01234 56789").append("\n")
                    .append(seperatorStrong).append("\n");

            // Ausgabe auf die Konsole
            System.out.println("===== KASSENBON (TEST) =====");
            System.out.println(consoleBon.toString());
            System.out.println("===========================");

            // Reset receipt data after printing
            receipt = new StringBuilder();
            stornos = new StringBuilder();
            totalAmount = 0.0;
            totalItems = 0;

        } catch (Exception ex) {
            Logger.getLogger(PrintingServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Helper class for counting product quantity
     */
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
}