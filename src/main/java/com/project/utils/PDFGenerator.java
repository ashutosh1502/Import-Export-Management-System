package com.project.utils;

import com.project.models.ExportBill;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.project.models.ExportBillTableEntry;
import com.project.models.StatementBill;
import com.project.models.StatementBillTableEntry;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class PDFGenerator {
    public PDFGenerator(){}

    public static String getSaveLocation(Stage stage, String defaultFilename){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Statement");

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)","*.pdf");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName(defaultFilename);
        File selectedFile = fileChooser.showSaveDialog(stage);

        if(selectedFile!=null){
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    public static void generateExportBillPDF(String filePath, ExportBill bill) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font tableDataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        Paragraph title = new Paragraph((bill.industryName==null)?"":bill.industryName.toUpperCase(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph addressLine = new Paragraph((bill.address==null)?"":bill.address,normalFont);
        addressLine.setAlignment(Element.ALIGN_CENTER);
        document.add(addressLine);

        Paragraph phoneNumberLine = new Paragraph((bill.contact==null)?"": bill.contact,normalFont);
        phoneNumberLine.setAlignment(Element.ALIGN_CENTER);
        document.add(phoneNumberLine);
        document.add(Chunk.NEWLINE);

        PdfPTable customerInfo = new PdfPTable(2);
        customerInfo.setWidthPercentage(100);
        customerInfo.setWidths(new float[]{2, 2});

        customerInfo.addCell(getLabelCell("Customer Name: "+((bill.customerName==null)?"Unknown":bill.customerName), labelFont));
        customerInfo.addCell(getLabelCell("Address: "+((bill.customerAddress==null)?"":bill.customerAddress), labelFont));
        customerInfo.addCell(getLabelCell("Phone No: "+((bill.customerPhoneNumber==null)?"":bill.customerPhoneNumber), labelFont));
        customerInfo.addCell(getLabelCell("Date: "+ LocalDate.now(), labelFont));
        customerInfo.addCell(getLabelCell("Invoice No: "+ ((bill.invoiceNumber==null)?"":bill.invoiceNumber), labelFont));
        customerInfo.addCell(getLabelCell("", labelFont));

        document.add(customerInfo);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.4f, 3f, 1.4f, 0.5f, 1.2f,0.5f,1,1,1.5f});

        table.addCell(getHeaderCell("Date", headerFont));
        table.addCell(getHeaderCell("Product Name", headerFont));
        table.addCell(getHeaderCell("Product ID", headerFont));
        table.addCell(getHeaderCell("Qty", headerFont));
        table.addCell(getHeaderCell("Taxable Amount", headerFont));
        table.addCell(getHeaderCell("GST %", headerFont));
        table.addCell(getHeaderCell("CGST", headerFont));
        table.addCell(getHeaderCell("SGST", headerFont));
        table.addCell(getHeaderCell("Net Amount", headerFont));

        double grandTotalVal=0.0, cgst, sgst, netAmount;
        for (ExportBillTableEntry entry: bill.tableEntries) {
            table.addCell(getValueCell(entry.date, tableDataFont, ""));
            table.addCell(getValueCell(entry.productName, tableDataFont, ""));
            table.addCell(getValueCell(entry.productId, tableDataFont, ""));
            table.addCell(getValueCell(Integer.toString(entry.quantity), tableDataFont, "right"));
            table.addCell(getValueCell(Double.toString(entry.taxableAmount), tableDataFont, "right"));
            table.addCell(getValueCell(Integer.toString(entry.gst),tableDataFont, "right"));
            cgst = entry.taxableAmount * ((double)(entry.gst/2)/100);
            sgst = cgst;
            netAmount = entry.taxableAmount + cgst + sgst;
            table.addCell(getValueCell(String.format("%.2f",cgst), tableDataFont, "right"));
            table.addCell(getValueCell(String.format("%.2f",sgst), tableDataFont, "right"));
            table.addCell(getValueCell(String.format("%.2f",netAmount), tableDataFont, "right"));
            grandTotalVal += netAmount;
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        double totalPaidVal = 0.0, totalPendingVal = 0.0;
        if(bill.paymentStatus.equalsIgnoreCase("paid"))
                totalPaidVal = grandTotalVal;
        else
                totalPendingVal = grandTotalVal;
        Paragraph grandTotal = new Paragraph("Grand Total: "+String.format("%.2f",grandTotalVal), labelFont);
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

    public static void generateStatementsPDF(String filePath, StatementBill bill) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font tableDataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
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

        Paragraph dateRangeLine = new Paragraph((bill.dateRange == null ||
                bill.dateRange.trim().equalsIgnoreCase("to")) ? "" : bill.dateRange,normalFont);
        dateRangeLine.setAlignment(Element.ALIGN_CENTER);
        document.add(Chunk.NEWLINE);
        document.add(dateRangeLine);
        document.add(Chunk.NEWLINE);

        PdfPTable statementInfoTable = new PdfPTable(5);
        statementInfoTable.setWidthPercentage(100);
        statementInfoTable.setWidths(new float[]{1.5f, 1.5f, 2, 1, 1});

        statementInfoTable.addCell(getHeaderCell("Date", headerFont));
        statementInfoTable.addCell(getHeaderCell("Tran. Type", headerFont));
        statementInfoTable.addCell(getHeaderCell("Invoice No.", headerFont));
        statementInfoTable.addCell(getHeaderCell("Debit", headerFont));
        statementInfoTable.addCell(getHeaderCell("Credit", headerFont));

        double debitTotal = 0.0, creditTotal = 0.0;
        for (StatementBillTableEntry entry: bill.tableEntries) {
            statementInfoTable.addCell(getStmtValueCell(entry.date, tableDataFont));
            statementInfoTable.addCell(getStmtValueCell(entry.transType, headerFont));
            statementInfoTable.addCell(getStmtValueCell(entry.invoiceNum, tableDataFont));
            statementInfoTable.addCell(getStmtValueCell(Double.toString(entry.debit), tableDataFont));
            statementInfoTable.addCell(getStmtValueCell(
                    (Double.toString(entry.credit).equalsIgnoreCase("0.0")) ? "" : Double.toString(entry.credit),
                    tableDataFont));
            debitTotal += entry.debit;
            creditTotal += entry.credit;
        }
        double closingBalance = debitTotal - creditTotal;
        String drcr = "Dr";
        if(closingBalance < 0) {
            drcr = "Cr";    closingBalance = Math.abs(closingBalance);
        }
        statementInfoTable.addCell(getStmtValueCell("",tableDataFont));
        statementInfoTable.addCell(getStmtValueCell("",tableDataFont));
        statementInfoTable.addCell(getStmtValueCell("",tableDataFont));
        statementInfoTable.addCell(getValueCell(Double.toString(debitTotal),tableDataFont, "right"));
        statementInfoTable.addCell(getValueCell(Double.toString(creditTotal),tableDataFont, "right"));
        statementInfoTable.addCell(getStmtValueCell("",tableDataFont));
        statementInfoTable.addCell(getStmtValueCell(drcr,tableDataFont));
        statementInfoTable.addCell(getStmtValueCell("Closing Balance",labelFont));
        statementInfoTable.addCell(getStmtValueCell("",tableDataFont));
        statementInfoTable.addCell(getStmtValueCell(Double.toString(closingBalance),labelFont));

        document.add(statementInfoTable);
        document.add(Chunk.NEWLINE);


        Paragraph authSignature = new Paragraph("Authorized Signature", labelFont);
        authSignature.setAlignment(Element.ALIGN_LEFT);
        document.add(Chunk.NEWLINE);
        document.add(authSignature);
        document.close();
    }

// Helper methods

    private static PdfPCell getHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
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

    private static PdfPCell getValueCell(String text, Font font, String alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if(alignment.equalsIgnoreCase("right"))
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell getStmtValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }


}
