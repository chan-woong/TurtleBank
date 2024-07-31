package com.app.turtlebank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQR extends AppCompatActivity {

    private QRCodeHandler qrCodeHandler;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_q_r);

        qrCodeHandler = new QRCodeHandler(this);

        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            Log.v("result", "result: " + result);
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                Log.d("data", "result.getContents(): " + result.getContents());
            } else {
                String qrData = result.getContents();
                String accountNumber = qrCodeHandler.getAccountNumber(qrData);

                if (qrCodeHandler.isQRCodeExpired(qrData)) {
                    Toast.makeText(this, "QR 유효 시간이 지났습니다.", Toast.LENGTH_LONG).show();

                    try {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Intent intent = new Intent(this, QR_sendMoney.class);
                    intent.putExtra("account_number", accountNumber);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
            Toast.makeText(this, "else", Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}