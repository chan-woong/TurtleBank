package com.app.turtlebank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;

//
public class CreateQR extends AppCompatActivity {

    private ImageView iv;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_q_r);

        iv = findViewById(R.id.qrcode);

        // Intent로 전달된 데이터 받기
        Intent intent = getIntent();
        if (intent != null) {
            String accountNumber = intent.getStringExtra("account_number");
            if (accountNumber != null && !accountNumber.isEmpty()) {

                String time_data = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
                text =accountNumber; // 예시로 출력
                generateQRCode(text+"&"+time_data);
            } else {
                Log.e("CreateQR", "No account number received in intent.");
            }
        } else {
            Log.e("CreateQR", "Intent is null.");
        }
    }

    private void generateQRCode(String text) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            iv.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("CreateQR", "QR code generation failed: " + e.toString());
        }
    }
}
