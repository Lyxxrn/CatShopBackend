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

        // Use the ReceiptBuilder
        ReceiptBuilder.ReceiptData receiptData = new ReceiptBuilder()
                .withCashier(cashierName)
                .withPaymentMethod(paymentMethod)
                .withProducts(products)
                .build();

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
            escpos.writeLF(title, receiptData.getSeperatorStrongTitle());
            for (String line : receiptData.getTitleParts()) {
                escpos.writeLF(title, line);
            }
            escpos.writeLF(title, receiptData.getSeperatorStrongTitle());

            // Cat image
            for (String line : receiptData.getCatImgParts()) {
                escpos.writeLF(catImg, line);
            }

            // Date and cashier information
            escpos.writeLF(receiptData.getSeperatorStrong())
                    .writeLF("Datum: " + java.time.LocalDate.now())
                    .writeLF("Uhrzeit: " + java.time.LocalTime.now().withNano(0))
                    .writeLF("Kassierer: " + receiptData.getCashierName())
                    .writeLF(receiptData.getSeperatorStrong())
                    .writeLF("Artikel   Preis Menge Total")
                    .writeLF(receiptData.getSeperatorWeak());

            // Regular product items
            escpos.write(receiptData.getReceiptContent());

            // Stornos if any
            if (!receiptData.getStornosContent().isEmpty()) {
                escpos.writeLF(receiptData.getSeperatorWeak())
                        .writeLF(bold, "STORNIERTE ARTIKEL")
                        .writeLF(receiptData.getSeperatorWeak())
                        .write(receiptData.getStornosContent());
            }

            // Total
            double mwst = receiptData.getTotalAmount() * 0.19;
            double totalWithTax = receiptData.getTotalAmount() + mwst;

            escpos.writeLF(receiptData.getSeperatorWeak())
                    .writeLF(bold, String.format("Gesamt          %2d %7.2f EUR",
                            receiptData.getTotalItems(), receiptData.getTotalAmount()))
                    .writeLF(receiptData.getSeperatorStrong())
                    .writeLF(String.format("MwSt. (19%%)  %.2f EUR", mwst))
                    .writeLF(receiptData.getSeperatorStrong())
                    .writeLF("Zahlungsmethode: " + receiptData.getPaymentMethod())
                    .writeLF(receiptData.getSeperatorStrong())
                    .writeLF("Vielen Dank für Ihren Einkauf!")
                    .writeLF("Besuchen Sie uns bald wieder im")
                    .writeLF("HealthyCat Shop!")
                    .writeLF(receiptData.getSeperatorStrong())
                    .writeLF("HealthyCat Shop")
                    .writeLF("Musterstraße 123")
                    .writeLF("12345 Musterstadt")
                    .writeLF("Tel: 01234 56789")
                    .writeLF(receiptData.getSeperatorStrong())
                    .feed(5)
                    .cut(EscPos.CutMode.FULL);

            escpos.close();

        } catch (IOException ex) {
            Logger.getLogger(PrintingService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}