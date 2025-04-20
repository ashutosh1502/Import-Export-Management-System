package com.project.utils;

import com.project.models.ExportBill;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.project.models.ExportBillTableEntry;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class PDFGenerator {
    public PDFGenerator(){}

    public static String getSaveLocation(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Statement");

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)","*.pdf");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File selectedFile = fileChooser.showSaveDialog(stage);

        if(selectedFile!=null){
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    public static void generateExportBillPDF(String filePath, ExportBill bill) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font tableDataFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        Paragraph title = new Paragraph(bill.industryName.toUpperCase(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph addressLine = new Paragraph(bill.address,normalFont);
        addressLine.setAlignment(Element.ALIGN_CENTER);
        document.add(addressLine);

        Paragraph phoneNumberLine = new Paragraph(bill.contact,normalFont);
        phoneNumberLine.setAlignment(Element.ALIGN_CENTER);
        document.add(phoneNumberLine);
        document.add(Chunk.NEWLINE);

        PdfPTable customerInfo = new PdfPTable(2);
        customerInfo.setWidthPercentage(100);
        customerInfo.setWidths(new float[]{2, 2});

        customerInfo.addCell(getLabelCell("Customer Name: "+bill.customerName, labelFont));
        customerInfo.addCell(getLabelCell("Address: "+bill.customerAddress, labelFont));
        customerInfo.addCell(getLabelCell("Phone No: "+bill.customerPhoneNumber, labelFont));
        customerInfo.addCell(getLabelCell("Date: "+ LocalDate.now(), labelFont));
        customerInfo.addCell(getLabelCell("Invoice No: "+ bill.invoiceNumber, labelFont));
        customerInfo.addCell(getLabelCell("", labelFont));

        document.add(customerInfo);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 4, 2, 1, 2});

        table.addCell(getHeaderCell("Date", headerFont, BaseColor.BLACK));
        table.addCell(getHeaderCell("Product Name", headerFont, BaseColor.BLACK));
        table.addCell(getHeaderCell("Product ID", headerFont, BaseColor.BLACK));
        table.addCell(getHeaderCell("Qty", headerFont, BaseColor.BLACK));
        table.addCell(getHeaderCell("Total Amt.", headerFont, BaseColor.BLACK));

        double grandTotalVal=0.0;
        for (ExportBillTableEntry entry: bill.tableEntries) {
            table.addCell(getValueCell(entry.date, tableDataFont));
            table.addCell(getValueCell(entry.productName, tableDataFont));
            table.addCell(getValueCell(entry.productId, tableDataFont));
            table.addCell(getValueCell(Integer.toString(entry.quantity), tableDataFont));
            table.addCell(getValueCell(Double.toString(entry.totalAmount), tableDataFont));
            grandTotalVal += entry.totalAmount;
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        double totalPaidVal = 0.0, totalPendingVal = 0.0;
        if(bill.paymentStatus.equalsIgnoreCase("paid"))
                totalPaidVal = grandTotalVal;
        else
                totalPendingVal = grandTotalVal;
        Paragraph grandTotal = new Paragraph("Grand Total: "+grandTotalVal, labelFont);
        grandTotal.setAlignment(Element.ALIGN_RIGHT);
        Paragraph br = new Paragraph("______________________", labelFont);
        br.setAlignment(Element.ALIGN_RIGHT);
        Paragraph totalPaid = new Paragraph("Total Paid: "+totalPaidVal, labelFont);
        totalPaid.setAlignment(Element.ALIGN_RIGHT);
        Paragraph totalPending = new Paragraph("Total Pending: "+totalPendingVal, labelFont);
        totalPending.setAlignment(Element.ALIGN_RIGHT);
        Paragraph authSignature = new Paragraph("Authorized Signature", labelFont);
        authSignature.setAlignment(Element.ALIGN_LEFT);

        document.add(grandTotal);
        document.add(br);
        document.add(totalPaid);
        document.add(totalPending);
        document.add(Chunk.NEWLINE);
        document.add(authSignature);
        document.close();
    }

// Helper methods

    private static PdfPCell getHeaderCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private static PdfPCell getLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell getValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        //cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }


}
