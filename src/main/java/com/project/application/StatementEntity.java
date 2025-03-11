package com.project.application;

public class StatementEntity {
    private int srno;
    private String type;
    private String invoiceNo;
    private String SCName;
    private String SCId;
    private double subTotal;
    private String paymentStatus;
    private String invoiceDate;

    public StatementEntity(int srno, String type, String invoiceNo, String SCName, String SCId, double subTotal, String status, String invoiceDate){
        this.srno = srno;
        this.type = type;
        this.invoiceNo = invoiceNo;
        this.SCName = SCName;
        this.SCId = SCId;
        this.subTotal = subTotal;
        this.paymentStatus = status;
        this.invoiceDate = invoiceDate;
    }

    public int getSrno() {
        return srno;
    }

    public String getType() {
        return type;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getSCName() {
        return SCName;
    }

    public String getSCId() {
        return SCId;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }
}
