package com.project.models;

import java.util.ArrayList;

public class ExportBill {
    public String industryName, address, contact, invoiceNumber;
    public String customerName;
    public String customerAddress;
    public String customerPhoneNumber;
    public String paymentStatus;
    public ArrayList<ExportBillTableEntry> tableEntries;

    public ExportBill(String industryName, String address, String contact, String customerName, String customerAddress, String customerPhoneNumber, String invoiceNumber, ArrayList<ExportBillTableEntry> tableEntries, String paymentStatus) {
        this.industryName = industryName;
        this.address = address;
        this.contact = contact;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhoneNumber = customerPhoneNumber;
        this.invoiceNumber = invoiceNumber;
        this.tableEntries = tableEntries;
        this.paymentStatus = paymentStatus;
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

    public ArrayList<ExportBillTableEntry> getTableEntries() {
        return tableEntries;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }
}
