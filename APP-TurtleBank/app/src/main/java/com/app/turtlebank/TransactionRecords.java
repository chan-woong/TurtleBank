package com.app.turtlebank;
//

import java.text.DecimalFormat;

public class TransactionRecords {
    private String fromaccnt;
    private  String toaccnt;
    private  String amount;


    public TransactionRecords(){}
    public TransactionRecords(String fromacc, String toacc, String amount){
        this.fromaccnt=fromacc;
        this.toaccnt=toacc;
        this.amount=amount;
    }

    public String getFromaccnt() {
        return fromaccnt;
    }

    public void setFromaccnt(String fromaccnt) {
        this.fromaccnt = "보낸 분:\t\t"+fromaccnt;
    }

    public String getToaccnt() {
        return toaccnt;
    }

    public void setToaccnt(String toaccnt) {
        this.toaccnt = "받는 분:\t\t"+toaccnt;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        int num = Integer.parseInt(amount);
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        amount = decimalFormat.format(num);
        this.amount = "이체금액:\t\t"+amount + "원";
    }

}



