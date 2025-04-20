package com.project.models;

public class ExportBillTableEntry {
    public String date, productName, productId, paymentStatus;
    public int quantity;
    public double totalAmount;

    public ExportBillTableEntry(String date, String productName, String productId, int quantity, double totalAmount){
        this.date = date;
        this.productName = productName;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }
}
