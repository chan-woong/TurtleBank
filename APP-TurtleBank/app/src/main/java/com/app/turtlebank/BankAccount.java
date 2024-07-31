package com.app.turtlebank;


public class BankAccount {
    private String account_number;
    private String bank_code;
    private String username;
    private int balance;

    public BankAccount() {}

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(int account_number) {
        this.account_number = account_number+"";
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code=bank_code;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = "이름:\n"+username+"\n";
    }
}
