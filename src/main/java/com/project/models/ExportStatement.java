package com.project.models;

import java.util.ArrayList;

public class ExportStatement {
    public String industryName,address,phoneNumber;
    public String customerName,customerAddress,customerPhoneNumber;
    public ArrayList<ExportStatementTableEntry> tableEntries;
    public double grandTotal, totalPending, totalPaid;

    public ExportStatement(String industryName, String address, String phoneNumber, String customerName, String customerAddress, String customerPhoneNumber, ArrayList<ExportStatementTableEntry> tableEntries, double grandTotal, double totalPending, double totalPaid) {
        this.industryName = industryName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhoneNumber = customerPhoneNumber;
        this.tableEntries = tableEntries;
        this.grandTotal = grandTotal;
        this.totalPending = totalPending;
        this.totalPaid = totalPaid;
    }
}
