package com.example.helper;

import com.example.response.BillResponse;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PdfUtils {
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
    public static boolean generatePdfAndSave(List<BillResponse> billResponses, String... fileName) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName[0] + "-bill.pdf"));
            document.open();
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Phrase header = new Phrase("Invoice", headerFont);
            document.add(header);
            document.add(new Phrase("\n"));
            document.add(new Phrase("Date: " + ((fileName.length > 1) ? fileName[1] : calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR))));
            document.add(new Phrase("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Customer");
            table.addCell("Product Name");
            table.addCell("Quantity");
            table.addCell("Price");
            AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
            billResponses.forEach(billResponse -> {
                billResponse.getAvailableProduct().forEach(productResponse -> {
                    table.addCell(billResponse.getOrderedCustomerDetail().getName());
                    table.addCell(productResponse.getProductName());
                    table.addCell(productResponse.getQuantity().toString());
                    table.addCell(productResponse.getProductPrice().toString());
                    totalPrice.updateAndGet(v -> v + productResponse.getProductPrice() * productResponse.getQuantity());
                });
            });
            document.add(new Phrase("\n"));
            document.add(table);
            document.add(new Phrase("\n"));
            document.add(new Phrase());
            document.close();
            System.out.println("PDF generated successfully!");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] generatePurchaseHistoryPDF(List<BillResponse> billResponses, boolean dailyPurchaseBill) throws IOException, DocumentException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Phrase header = new Phrase("Invoice", headerFont);
            document.add(header);
            document.add(new Phrase("\n"));
            document.add(new Phrase("Date: " + calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR)));
            document.add(new Phrase("\n"));

            if (dailyPurchaseBill) {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.addCell("Customer");
                table.addCell("Product Name");
                table.addCell("Quantity");
                table.addCell("Price");
                AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
                billResponses.forEach(billResponse -> {
                    billResponse.getAvailableProduct().forEach(productResponse -> {
                        table.addCell(billResponse.getOrderedCustomerDetail().getName());
                        table.addCell(productResponse.getProductName());
                        table.addCell(productResponse.getQuantity().toString());
                        table.addCell(productResponse.getProductPrice().toString());
                        totalPrice.updateAndGet(v -> v + productResponse.getProductPrice() * productResponse.getQuantity());
                    });
                });
                document.add(new Phrase("\n"));
                document.add(table);
                document.add(new Phrase("\n"));
                document.add(new Phrase());
            }
            else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.addCell("Product Name");
                table.addCell("Quantity");
                table.addCell("Price");
                AtomicReference<Double> totalPrice = new AtomicReference<Double>(0.0);
                billResponses.forEach(billResponse -> {
                    try {
                        document.add(new Phrase("Customer: " + billResponse.getOrderedCustomerDetail().getName()));
                    } catch (DocumentException e) {
                        throw new RuntimeException(e);
                    }
                    billResponse.getAvailableProduct().forEach(productResponse -> {
                        table.addCell(productResponse.getProductName());
                        table.addCell(productResponse.getQuantity().toString());
                        table.addCell("Rs:" + productResponse.getProductPrice().toString());
                        totalPrice.updateAndGet(v -> v + productResponse.getProductPrice() * productResponse.getQuantity());
                    });
                });
                document.add(new Phrase("\n"));
                document.add(new Phrase("Total: Rs." + totalPrice.get()));
                document.add(table);
                document.add(new Phrase("\n"));
                document.add(new Phrase());
            }
            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }
}
