package com.app.turtlebank;
// 송금 Activity

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class Mydata_sendMoney extends AppCompatActivity {

    Button send;
    TextView tt;
    private long now;
    private Date date;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    // Enter the correct url for your api service site
    final int initialTimeoutMs = 2000; // 초기 타임아웃 값 (5초)
    final int maxNumRetries = 0; // 최대 재시도 횟수
    final float backoffMultiplier = 1f; // 재시도 간격의 배수

    RetryPolicy policy = new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydatasendmoney);
        tt = findViewById(R.id.edact);
        Intent i = getIntent();
        String accountNumber = i.getStringExtra("account_number");
        tt.setText(accountNumber);
        tt.setFocusable(false);  // 포커스 받지 않도록 설정
        tt.setClickable(false);  // 클릭 불가능하도록 설정
        tt.setCursorVisible(false);  // 커서 숨기기 (있는 경우)

        Spinner spinnerBank = findViewById(R.id.spinnerBank);

        // Spinner에 선택 옵션 추가
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.bank_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);

        send = findViewById(R.id.sendbutton);
        send.setOnClickListener(v -> sendMoney());

    }

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
    public void sendMoney() {
        int to_bankcode = getSelectedBankCode();
        EditText ed2 = findViewById(R.id.edact2);     // 수취계좌
        EditText ed4 = findViewById(R.id.edamt);    // 이체금액
        EditText ed5  = findViewById(R.id.accountPW);

        if (to_bankcode == 555) {
            SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
            final String retrivedToken = sharedPreferences.getString("accesstoken", null);
            SharedPreferences sharedPreferences1 = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences1.getString("apiurl", null);
            String endpoint = "/api/Mydata/b_to_a";
            final String finalUrl = url + endpoint;

            //EditText ed = findViewById(R.id.edact);     // 송금계좌
            int from_bankcode = 0;
            int from_account = 0;
            int to_account = 0;
            int amount = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            now = System.currentTimeMillis();
            date = new Date(now);
            String sendtime = sdf.format(date);
            String accountPW = ed5.getText().toString().trim();
            String hAccountPW = hPassword(accountPW);

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();
            try {
                // fetch values
                if (!ed2.getText().toString().isEmpty() && !ed4.getText().toString().isEmpty() && !ed5.getText().toString().isEmpty()) {
                    from_bankcode = 333;
                    from_account = Integer.parseInt(tt.getText().toString());
                    to_account = Integer.parseInt(ed2.getText().toString());
                    amount = Integer.parseInt(ed4.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Input ", Toast.LENGTH_SHORT).show();
                    onRestart();
                }

                // input your API parameters
                requestData.put("from_account", from_account);  // 송금계좌 varchar
                requestData.put("bank_code", to_bankcode);      // 수취계좌 varchar
                requestData.put("to_account", to_account);      // 수취계좌 varchar
                requestData.put("amount", amount);              // 이체금액 int
                requestData.put("sendtime", sendtime);          // 전송시간 datetime
                requestData.put("hAccountPW",hAccountPW);

                Log.d("formmmmmmmmmmmm", requestData.toString());

                // Encrypt data before sending
                requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Enter the correct url for your api service site
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, requestDataEncrypted,
                    response -> {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                            Log.d("Send Money", decryptedResponse.toString());

                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(getApplicationContext(), "" + EncryptDecrypt.decrypt(response.get("enc_data").toString()), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }, error -> Toast.makeText(getApplicationContext(), "Something went wrong[Send]", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("Authorization", "Bearer " + retrivedToken);
                    return headers;
                }
                @Override
                public RetryPolicy getRetryPolicy() {
                    // RetryPolicy 설정
                    return policy;
                }
            };

            requestQueue.add(jsonObjectRequest);
        }

        else {
            SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
            final String retrivedToken = sharedPreferences.getString("accesstoken", null);
            SharedPreferences sharedPreferences1 = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences1.getString("apiurl", null);
            String endpoint = "/api/Mydata/b_to_b";
            final String finalUrl = url + endpoint;

            //EditText ed = findViewById(R.id.edact);     // 송금계좌
            int from_bankcode = 0;
            int from_account = 0;
            int to_account = 0;
            int amount = 0;
            String sendtime = dateFormat.format(date);
            String accountPW = ed5.getText().toString().trim();
            String hAccountPW = hPassword(accountPW);

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();
            try {
                // fetch values
                if (!ed2.getText().toString().isEmpty() && !ed4.getText().toString().isEmpty() && !ed5.getText().toString().isEmpty()) {
                    from_bankcode = 333;
                    from_account = Integer.parseInt(tt.getText().toString());
                    to_account = Integer.parseInt(ed2.getText().toString());
                    amount = Integer.parseInt(ed4.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Input ", Toast.LENGTH_SHORT).show();
                    onRestart();
                }

                // input your API parameters
                requestData.put("from_account", from_account);  // 송금계좌 varchar
                requestData.put("bank_code", to_bankcode);      // 수취계좌 varchar
                requestData.put("to_account", to_account);      // 수취계좌 varchar
                requestData.put("amount", amount);              // 이체금액 int
                requestData.put("sendtime", sendtime);          // 전송시간 datetime
                requestData.put("hAccountPW",hAccountPW);

                Log.d("formmmmmmmmmmmm", requestData.toString());

                // Encrypt data before sending
                requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Enter the correct url for your api service site
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, requestDataEncrypted,
                    response -> {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                            Log.d("Send Money", decryptedResponse.toString());

                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Toast.makeText(getApplicationContext(), "" + EncryptDecrypt.decrypt(response.get("enc_data").toString()), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }, error -> Toast.makeText(getApplicationContext(), "Something went wrong[Send]", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("Authorization", "Bearer " + retrivedToken);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);
        }

    }

    private int getSelectedBankCode() {
        Spinner spinnerBank = findViewById(R.id.spinnerBank);

        // 선택된 뱅크의 위치(index)를 가져옴
        int selectedPosition = spinnerBank.getSelectedItemPosition();

        // 선택된 뱅크 옵션의 배열에서 해당 위치의 문자열을 가져옴
        String[] bankOptions = getResources().getStringArray(R.array.bank_options);
        String selectedBank = bankOptions[selectedPosition];

        // 선택된 뱅크에 따라 코드를 할당하거나 다른 처리를 수행
        int to_bankcode = 0;
        if ("TurtleBank".equals(selectedBank)) {
            to_bankcode = 555; // 예시 코드, 실제 뱅크 코드에 따라 수정
        } else if ("RabbitBank".equals(selectedBank)) {
            to_bankcode = 333; // 예시 코드, 실제 뱅크 코드에 따라 수정
        }
        // 다른 뱅크에 대한 처리도 추가 가능

        return to_bankcode;
    }

    // ... (나머지 코드)
}


