package com.project.application;

import java.util.ArrayList;

public class Exports {
    private final String SrNo;
    private final String invoiceNo;
    private final String customerName;
    private final ArrayList<String> products;
    private final int quantity;
    private final double price;
    private final String invoiceDate;

    public Exports(int srno,String invoiceNo,String supplierName, ArrayList<String> products, int quantity, float price, String date) {
        this.SrNo=srno+".";
        this.invoiceNo=invoiceNo;
        this.customerName = supplierName;
        this.products = products;
        this.quantity = quantity;
        this.price = price;
        this.invoiceDate = date;
    }

    public String getSrNo(){ return SrNo; }

    public String getInvoiceNo(){return invoiceNo;}

    public String getCustomerName() {
        return customerName;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}