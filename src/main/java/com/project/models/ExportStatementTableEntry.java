package com.project.models;

public class ExportStatementTableEntry {
    public String date, invoiceNumber, productName, productId, paymentStatus;
    public int quantity;
    public double totalAmount;

    public ExportStatementTableEntry(String date, String invoiceNumber, String productName, String productId, int quantity, double totalAmount, String paymentStatus){
        this.date = date;
        this.invoiceNumber = invoiceNumber;
        this.productName = productName;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }
}
