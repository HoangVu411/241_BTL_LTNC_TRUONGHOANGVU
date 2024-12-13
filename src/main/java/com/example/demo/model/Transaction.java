package com.example.demo.model;

public class Transaction {

    private String dateTime;
    private int transNo;
    private int credit;
    private int debit;
    private String detail;
   

    // Constructor
    public Transaction(String dateTime, int transNo, int credit, int debit, String detail) {
        this.dateTime = dateTime;
        this.transNo = transNo;
        this.credit = credit;
        this.debit = debit;
        this.detail = detail;
    }

    // Getters and Setters
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getTransNo() {
        return transNo;
    }

    public void setTransNo(int transNo) {
        this.transNo = transNo;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getDebit() {
        return debit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

 
}
