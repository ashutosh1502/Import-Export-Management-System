package com.project.models;

import java.util.ArrayList;

public class StatementBill {
    public String industryName, address, contact;
    public String dateRange;
    public ArrayList<StatementBillTableEntry> tableEntries;

    public StatementBill(String industryName, String address, String contact, String dateRange, ArrayList<StatementBillTableEntry> tableEntries) {
        this.industryName = industryName;
        this.address = address;
        this.contact = contact;
        this.dateRange = dateRange;
        this.tableEntries = tableEntries;
    }
}
