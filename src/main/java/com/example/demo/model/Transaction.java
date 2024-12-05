package com.example.demo.model;

public class Transaction {

    private String dateTime;
    private int transNo;
    private double credit;
    private double debit;
    private String detail;

    // Constructor
    public Transaction(String dateTime, int transNo, double credit, double debit, String detail) {
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

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
