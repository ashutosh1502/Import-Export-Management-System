package com.project.application;

public class StatementEntity {
    private int srno;
    private String type;
    private String invoiceNo;
    private String SCName;
    private String SCId;
    private int totalQty;
    private double subTotal;
    private String paymentStatus;

    public StatementEntity(int srno,String type,String invoiceNo,String SCName,String SCId,int totalQty,double subTotal,String status){
        this.srno = srno;
        this.type = type;
        this.invoiceNo = invoiceNo;
        this.SCName = SCName;
        this.SCId = SCId;
        this.totalQty = totalQty;
        this.subTotal = subTotal;
        this.paymentStatus = status;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getSCName() {
        return SCName;
    }

    public void setSCName(String SCName) {
        this.SCName = SCName;
    }

    public String getSCId() {
        return SCId;
    }

    public void setSCId(String SCId) {
        this.SCId = SCId;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
