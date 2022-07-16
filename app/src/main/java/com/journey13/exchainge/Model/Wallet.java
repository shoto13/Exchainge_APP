package com.journey13.exchainge.Model;

public class Wallet {

    private Float wBalance;
    private Float wMonthlyEarnings;


    public Wallet(Float wBalance, Float wMonthlyEarnings) {
        this.wBalance = wBalance;
        this.wMonthlyEarnings = wMonthlyEarnings;

    }

    public Wallet() {

    }

    public Float getwBalance() {
        return wBalance;
    }
    public void setwBalance(Float wBalance) {
        this.wBalance = wBalance;
    }

    public Float getwMonthlyEarnings() {
        return wMonthlyEarnings;
    }
    public void setwMonthlyEarnings(Float wMonthlyEarnings) {
        this.wMonthlyEarnings = wMonthlyEarnings;
    }
}