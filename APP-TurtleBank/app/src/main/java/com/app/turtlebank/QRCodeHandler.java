package com.app.turtlebank;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class QRCodeHandler {
    private Context context;

    public QRCodeHandler(Context context) {
        this.context = context;
    }

    // Method to parse QR code data and get account number
    public String getAccountNumber(String qrData) {
        return qrData.split("&")[0];
    }

    // Method to check if the QR code is expired
    public boolean isQRCodeExpired(String qrData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date timeOutDate;
        Date currentDate;
        try {
            timeOutDate = dateFormat.parse(qrData.split("&")[1]);
            currentDate = dateFormat.parse(dateFormat.format(new Date()));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeOutDate);
            calendar.add(Calendar.MINUTE, 1); // Add 1 minute to the QR code creation time
            timeOutDate = new Date(calendar.getTimeInMillis());

            return currentDate.after(timeOutDate);
        } catch (ParseException e) {
            Toast.makeText(context, "ParseException: " + e, Toast.LENGTH_LONG).show();
            return true;
        }
    }
}