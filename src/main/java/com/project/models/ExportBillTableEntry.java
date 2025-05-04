package com.project.models;

public class ExportBillTableEntry {
    public String date, productName, productId, paymentStatus;
    public int quantity, gst;
    public double taxableAmount;

    public ExportBillTableEntry(String date, String productName, String productId, int quantity, double taxableAmount, int gst){
        this.date = date;
        this.productName = productName;
        this.productId = productId;
        this.quantity = quantity;
        this.taxableAmount = taxableAmount;
        this.gst = gst;
    }
}
