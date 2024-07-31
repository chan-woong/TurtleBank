package com.app.turtlebank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class QRMainActivity extends AppCompatActivity {
    private Button createQRBtn;
    private Button scanQRBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account_list);

        createQRBtn = (Button) findViewById(R.id.show_QR);

        createQRBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(QRMainActivity.this, CreateQR.class);
                startActivity(intent);
            }
        });


    }
}
