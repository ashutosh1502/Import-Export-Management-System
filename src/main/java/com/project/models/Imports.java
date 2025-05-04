package com.project.models;

import java.util.ArrayList;

public class Imports {
    private final String SrNo;
    private final String invoiceNo;
    private final String supplierName;
    private final String supplierId;
    private final ArrayList<String> products;
    private final int totalQuantity;
    private final double netTotal;
    private final String address,city,state,phno,email,orderDate,invoiceDate,paymentMode,paymentStatus;

    public Imports(int srno, String invoiceNo, String supplierId, String supplierName, ArrayList<String> products, int totalQuantity, double netTotal, String address, String city, String state, String phno, String email, String orderDate, String invoiceDate, String paymentMode, String paymentStatus) {
        this.SrNo=srno+".";
        this.invoiceNo = invoiceNo;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.products = products;
        this.totalQuantity = totalQuantity;
        this.netTotal = netTotal;
        this.address = address;
        this.city = city;
        this.state = state;
        this.phno = phno;
        this.email = email;
        this.orderDate = orderDate;
        this.invoiceDate = invoiceDate;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
    }

    public String getSrNo(){ return SrNo; }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }
    public String getSupplierId() {
        return supplierId;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getPhno() {
        return phno;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}