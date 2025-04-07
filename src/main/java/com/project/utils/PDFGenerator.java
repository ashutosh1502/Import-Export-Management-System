package com.project.utils;

import com.project.models.ExportStatement;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;

public class PDFGenerator {
    public PDFGenerator(){}

    public static File getSaveLocation(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Statement");

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)","*.pdf");
        fileChooser.getExtensionFilters().add(extensionFilter);

        return fileChooser.showSaveDialog(stage);
    }

    public void generateExportStatementPDF(String filePath, ExportStatement statement) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        Paragraph title = new Paragraph(statement.industryName.toUpperCase(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph addressLine = new Paragraph(statement.address,normalFont);
        addressLine.setAlignment(Element.ALIGN_CENTER);
        document.add(addressLine);

        Paragraph phoneNumberLine = new Paragraph(statement.phoneNumber,normalFont);
        phoneNumberLine.setAlignment(Element.ALIGN_CENTER);
        document.add(phoneNumberLine);
        document.add(Chunk.NEWLINE);

        PdfPTable customerInfo = new PdfPTable(2);
        customerInfo.setWidthPercentage(100);
        customerInfo.setWidths(new float[]{1, 2});

        customerInfo.addCell(getLabelCell("Customer Name:", labelFont));
        customerInfo.addCell(getLabelCell("Address:", labelFont));
        customerInfo.addCell(getLabelCell("Invoice No:", labelFont));
        customerInfo.addCell(getLabelCell("Date:", labelFont));

        document.add(customerInfo);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 3, 2, 1, 2});

        BaseColor headerColor = new BaseColor(63, 81, 181);

        table.addCell(getHeaderCell("Date", headerFont, headerColor));
        table.addCell(getHeaderCell("Invoice No.", headerFont, headerColor));
        table.addCell(getHeaderCell("Product Name", headerFont, headerColor));
        table.addCell(getHeaderCell("Product ID", headerFont, headerColor));
        table.addCell(getHeaderCell("Qty", headerFont, headerColor));
        table.addCell(getHeaderCell("Total Amt.", headerFont, headerColor));

//        for (Product p : statement.getProducts()) {
//            table.addCell(getValueCell(p.getProductName(), normalFont));
//            table.addCell(getValueCell(String.valueOf(p.getQuantity()), normalFont));
//            table.addCell(getValueCell("₹" + p.getPrice(), normalFont));
//            table.addCell(getValueCell("₹" + (p.getQuantity() * p.getPrice()), normalFont));
//            table.addCell(getValueCell(p.getExportDate().toString(), normalFont));
//        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        Paragraph grandTotal = new Paragraph("Grand Total:", labelFont);
        grandTotal.setAlignment(Element.ALIGN_RIGHT);
        Paragraph totalPaid = new Paragraph("Total Paid:", labelFont);
        totalPaid.setAlignment(Element.ALIGN_RIGHT);
        Paragraph totalPending = new Paragraph("Total Pending:", labelFont);
        totalPending.setAlignment(Element.ALIGN_RIGHT);
        Paragraph authSignature = new Paragraph("Authorized Signature", labelFont);
        authSignature.setAlignment(Element.ALIGN_LEFT);

        document.add(grandTotal);
        document.add(totalPaid);
        document.add(totalPending);
        document.add(Chunk.NEWLINE);
        document.add(authSignature);
        document.close();
    }

// Helper methods

    private PdfPCell getHeaderCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell getLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell getValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }


}
