package com.project.models;

public class StatementBillTableEntry {
    public String date, transType, invoiceNum;
    public double debit = 0.0, credit = 0.0, subtotal;

    public StatementBillTableEntry(String date, String transType, String invoiceNum, double subtotal) {
        this.date = date;
        this.transType = transType;
        this.invoiceNum = invoiceNum;
        this.subtotal = subtotal;
        if(transType.equalsIgnoreCase("imports"))
            debit = subtotal;
        else
            credit = subtotal;
    }
}
