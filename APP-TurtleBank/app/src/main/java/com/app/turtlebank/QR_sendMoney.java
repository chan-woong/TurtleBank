package com.app.turtlebank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class QR_sendMoney extends AppCompatActivity {

    Button send;
    TextView tt;
    private long now;
    private Date date;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected static String hPassword(String accountPW) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(accountPW.getBytes());

            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Enter the correct url for your api service site
    final int initialTimeoutMs = 2000; // 초기 타임아웃 값 (5초)
    final int maxNumRetries = 0; // 최대 재시도 횟수
    final float backoffMultiplier = 1f; // 재시도 간격의 배수

    RetryPolicy policy = new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_sendmoney);
        tt = findViewById(R.id.edact2);
        Intent i = getIntent();
        String accountNumber = i.getStringExtra("account_number");
        tt.setText(accountNumber);
        tt.setFocusable(false);
        tt.setClickable(false);
        tt.setCursorVisible(false);

        send = findViewById(R.id.sendbutton);
        send.setOnClickListener(v -> sendMoney());
    }

    public void sendMoney() {
        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        SharedPreferences sharedPreferences1 = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences1.getString("apiurl", null);
        String endpoint = "/api/balance/transfer_app";
        final String finalUrl = url + endpoint;

        EditText ed = findViewById(R.id.edact);     // 송금계좌
        EditText ed2 = findViewById(R.id.edact2);   // 수취계좌
        EditText ed3 = findViewById(R.id.edamt);    // 이체금액
        EditText ed4 = findViewById(R.id.accountPW); // 계좌비번
        int from_account = 0;
        int to_account = 0;
        int amount = 0;

        String accountPW = ed4.getText().toString().trim();
        String hAccountPW = hPassword(accountPW);

        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        now = System.currentTimeMillis();
        date = new Date(now);
        String sendtime = dateFormat.format(date);

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {
            if (!ed.getText().toString().isEmpty() && !ed2.getText().toString().isEmpty() && !ed3.getText().toString().isEmpty()) {
                from_account = Integer.parseInt(ed.getText().toString());
                to_account = Integer.parseInt(ed2.getText().toString());
                amount = Integer.parseInt(ed3.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "Invalid Input ", Toast.LENGTH_SHORT).show();
                onRestart();
                return;
            }

            requestData.put("from_account", from_account);
            requestData.put("to_account", to_account);
            requestData.put("amount", amount);
            requestData.put("sendtime", sendtime);
            requestData.put("accountPW", hAccountPW);

            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("QR_sendMoney", "Request data: " + requestData.toString());
        Log.d("QR_sendMoney", "Encrypted request data: " + requestDataEncrypted.toString());

        sendTransferRequest(requestQueue, finalUrl, retrivedToken, requestDataEncrypted, from_account, sendtime, hAccountPW);
    }

    private void sendTransferRequest(RequestQueue requestQueue, String url, String token, JSONObject requestData, int from_account, String sendtime, String hAccountPW) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        Log.d("QR_sendMoney", "Response: " + response.toString());
                        JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                        Log.d("QR_sendMoney", "Decrypted response: " + decryptedResponse.toString());

                        if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                            Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendFeeTransfer(requestQueue, url, token, from_account, sendtime, hAccountPW);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.e("QR_sendMoney", "Error: ", error);
            Toast.makeText(getApplicationContext(), "Something went wrong[Send]", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
        requestQueue.getCache().clear();
    }

    private void sendFeeTransfer(RequestQueue requestQueue, String url, String token, int from_account, String sendtime, String hAccountPW) {
        int fee_amount = 1000;
        int fee_account = 999999;  // Administrator account

        JSONObject feeRequestData = new JSONObject();
        JSONObject feeRequestDataEncrypted = new JSONObject();

        try {
            feeRequestData.put("from_account", from_account);
            feeRequestData.put("to_account", fee_account);
            feeRequestData.put("amount", fee_amount);
            feeRequestData.put("sendtime", sendtime);
            feeRequestData.put("accountPW", hAccountPW);

            feeRequestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(feeRequestData.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("QR_sendMoney", "Fee request data: " + feeRequestData.toString());
        Log.d("QR_sendMoney", "Encrypted fee request data: " + feeRequestDataEncrypted.toString());

        final JsonObjectRequest feeRequest = new JsonObjectRequest(Request.Method.POST, url, feeRequestDataEncrypted,
                response -> {
                    try {
                        Log.d("QR_sendMoney", "Fee Response: " + response.toString());
                        JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                        Log.d("QR_sendMoney", "Decrypted fee response: " + decryptedResponse.toString());

                        if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                            Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(getApplicationContext(), "송금과 수수료 송금이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }, error -> {
            Log.e("QR_sendMoney", "Fee Error: ", error);
            Toast.makeText(getApplicationContext(), "Something went wrong[Fee Send]", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(feeRequest);
        requestQueue.getCache().clear();
    }
}
