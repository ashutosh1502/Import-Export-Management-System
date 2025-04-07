package com.project.models;

public class Product {
    private final String SrNo;
    private final String productName;
    private final String productID;
    private final int quantity;
    private final double price;

    public Product(int srNo,String productID,String productName, int quantity, double price) {
        this.SrNo=srNo+".";
        this.productID=productID;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getSrNo(){ return SrNo; }

    public String getProductID(){ return productID; }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}