package com.journey13.exchainge.Model;

public class Wallet {

    private Float wBalance;
    


    public Wallet(Float wBalance) {
        this.wBalance = wBalance;

    }

    public Wallet() {

    }

    public Float getwBalance() {
        return wBalance;
    }

    public void setwBalance(Float wBalance) {
        this.wBalance = wBalance;
    }

  }