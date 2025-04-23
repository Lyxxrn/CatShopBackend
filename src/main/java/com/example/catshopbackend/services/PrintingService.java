package com.example.catshopbackend.services;

import com.example.catshopbackend.dto.ProductDTO;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PrintingService {

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
            escpos = new EscPos(new PrinterOutputStream(printService));

            Style title = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center);

            Style catImg = new Style(escpos.getStyle())
                    .setBold(true)
                    .setJustification(EscPosConst.Justification.Center);

            Style bold = new Style(escpos.getStyle())
                    .setBold(true);

            // Header
            escpos.writeLF(title, seperatorStrongTitle);
            for (String line : titleParts) {
                escpos.writeLF(title, line);
            }
            escpos.writeLF(title, seperatorStrongTitle);

            // Cat image
            for (String line : catImgParts) {
                escpos.writeLF(catImg, line);
            }

            // Date and cashier information
            escpos.writeLF(seperatorStrong)
                    .writeLF("Datum: " + java.time.LocalDate.now())
                    .writeLF("Uhrzeit: " + java.time.LocalTime.now().withNano(0))
                    .writeLF("Kassierer: " + cashierName) // Already the SCO# username from authentication
                    .writeLF(seperatorStrong)
                    .writeLF("Artikel   Preis Menge Total")
                    .writeLF(seperatorWeak);

            // Regular product items
            escpos.write(receipt.toString());

            // Stornos if any
            if (stornos.length() > 0) {
                escpos.writeLF(seperatorWeak)
                        .writeLF(bold, "STORNIERTE ARTIKEL")
                        .writeLF(seperatorWeak)
                        .write(stornos.toString());
            }

            // Total
            double mwst = totalAmount * 0.19;
            double totalWithTax = totalAmount + mwst;

            escpos.writeLF(seperatorWeak)
                    .writeLF(bold, String.format("Gesamt          %2d %7.2f EUR", totalItems, totalAmount))
                    .writeLF(seperatorStrong)
                    .writeLF(String.format("MwSt. (19%%)  %.2f EUR", mwst))
                    .writeLF(String.format("Gesamt inkl. MwSt. %.2f EUR", totalWithTax))
                    .writeLF(seperatorStrong)
                    .writeLF("Zahlungsmethode: " + paymentMethod)
                    .writeLF(seperatorStrong)
                    .writeLF("Vielen Dank für Ihren Einkauf!")
                    .writeLF("Besuchen Sie uns bald wieder im")
                    .writeLF("HealthyCat Shop!")
                    .writeLF(seperatorStrong)
                    .writeLF("HealthyCat Shop")
                    .writeLF("Musterstraße 123")
                    .writeLF("12345 Musterstadt")
                    .writeLF("Tel: 01234 56789")
                    .writeLF(seperatorStrong)
                    .feed(5)
                    .cut(EscPos.CutMode.FULL);

            escpos.close();

            // Reset receipt data after printing
            receipt = new StringBuilder();
            stornos = new StringBuilder();
            totalAmount = 0.0;
            totalItems = 0;

        } catch (IOException ex) {
            Logger.getLogger(PrintingService.class.getName()).log(Level.SEVERE, null, ex);
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