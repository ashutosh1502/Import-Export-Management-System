package com.project.models;

import java.util.ArrayList;

public class ExportStatement {
    public String industryName,address, contact;
    public String customerName,customerAddress,customerPhoneNumber;
    public ArrayList<ExportStatementTableEntry> tableEntries;
    public double grandTotal, totalPending, totalPaid;

    public ExportStatement(String industryName, String address, String contact, String customerName, String customerAddress, String customerPhoneNumber, ArrayList<ExportStatementTableEntry> tableEntries, double grandTotal, double totalPending, double totalPaid) {
        this.industryName = industryName;
        this.address = address;
        this.contact = contact;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhoneNumber = customerPhoneNumber;
        this.tableEntries = tableEntries;
        this.grandTotal = grandTotal;
        this.totalPending = totalPending;
        this.totalPaid = totalPaid;
    }

    public String getIndustryName() {
        return industryName;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public ArrayList<ExportStatementTableEntry> getTableEntries() {
        return tableEntries;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public double getTotalPending() {
        return totalPending;
    }

    public double getTotalPaid() {
        return totalPaid;
    }
}
