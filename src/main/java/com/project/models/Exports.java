package com.project.models;

import java.util.ArrayList;

public class Exports {
    private final String SrNo;
    private final String invoiceNo;
    private final String customerName;
    private final String customerId;
    private final ArrayList<String> products;
    private final int totalQuantity;
    private final double netTotal;
    private final String address,city,state,phno,email,orderDate,invoiceDate,paymentMode,paymentStatus;

    public Exports(int srno, String invoiceNo, String customerId, String customerName, ArrayList<String> products, int totalQuantity, double netTotal, String address, String city, String state, String phno, String email, String orderDate, String invoiceDate, String paymentMode, String paymentStatus) {
        this.SrNo=srno+".";
        this.invoiceNo=invoiceNo;
        this.customerName = customerName;
        this.customerId = customerId;
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

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPhno() {
        return phno;
    }

    public String getEmail() {
        return email;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
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

    public double getNetTotal() {
        return netTotal;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }
}