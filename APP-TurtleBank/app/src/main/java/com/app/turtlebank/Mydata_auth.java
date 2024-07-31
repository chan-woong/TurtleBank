package com.app.turtlebank;
// 송금 Activity

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Mydata_auth extends AppCompatActivity {

    Button send;
    Button sendCancel;
    TextView tt;

    private JSONArray dataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OkHttpClient client2 = new OkHttpClient();
        EncryptDecrypt endecryptor2 = new EncryptDecrypt();
        SharedPreferences sharedPreferences = Mydata_auth.this.getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken2  = sharedPreferences.getString("accesstoken",null);

        String apiUrl2 = "http://m.turtle-bank.com/api/Account/view";

        RequestBody requestBody2 = new FormBody.Builder()
                .add("username", "username")
                .add("authnum","authnum")

                // 다른 필요한 데이터도 추가해주세요
                .build();

        String encryptedData2 = endecryptor2.encrypt(requestBody2.toString());
        RequestBody encryptedRequestBody = RequestBody.create(MediaType.parse("text/plain"), encryptedData2);

        okhttp3.Request request2 = new okhttp3.Request.Builder()
                .url(apiUrl2)
                .post(encryptedRequestBody)
                .addHeader("Authorization", "1 " + retrivedToken2)
                .build();



        client2.newCall(request2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 요청 실패 처리
                e.printStackTrace();
                Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                // 요청 성공 시 처리
                if (response.isSuccessful()) {
                    String responseData2 = response.body().string();
                    Log.d("API_RESPONSE", "JSON Response: " + responseData2);


                    // 응답 데이터 파싱
                    try {
                        JSONObject jsonResponse3 = new JSONObject(responseData2);
                        String encData2 = jsonResponse3.getString("enc_data");
                        String data2 = endecryptor2.decrypt(encData2);
                        Log.d("API_RESPONSE", "JSON Response: " + data2);

                        JSONObject dataObject2 = new JSONObject(data2);
                        dataArray = dataObject2.getJSONArray("data");
                        JSONObject firstObject = dataArray.getJSONObject(0);
                        String username = firstObject.getString("username");
                        Log.d("API_RESPONSE", "user 뽑아오기: " + username);
                        useusername(username);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                } else {
                    // 서버에서 오류 응답이 온 경우 처리
                    // response.code() 및 response.message()를 통해 상세한 정보를 얻을 수 있음
                }
            }
        });
        setContentView(R.layout.activity_mydata_auth);
        send = findViewById(R.id.buttonOk);
        sendCancel = findViewById(R.id.buttonCancel);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mydata_auth();
            }
        });
        sendCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 시 특정 activity로 이동하는 코드 작성
                finish();
            }
        });




    }

    private void useusername(String username){
        OkHttpClient client2 = new OkHttpClient();
        EncryptDecrypt endecryptor2 = new EncryptDecrypt();
        SharedPreferences sharedPreferences = Mydata_auth.this.getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken2  = sharedPreferences.getString("accesstoken",null);

        Log.d("API_RESPONSE", "log start: ");

        String apiUrl2 = "http://m.turtle-bank.com/api/Mydata/mydata_sms";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl2).newBuilder();
        urlBuilder.addQueryParameter("username", username);
        String finalUrl = urlBuilder.build().toString();

        okhttp3.Request request2 = new okhttp3.Request.Builder()
                .url(finalUrl)
                .get()
                .addHeader("Authorization", "1 " + retrivedToken2)
                .build();



        String encryptedData2 = endecryptor2.encrypt(request2.toString());

        Log.d("API_RESPONSE", "log start22222222: " + encryptedData2);
        Log.d("API_RESPONSE", "log 3333333333333333: " + request2);
        client2.newCall(request2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 요청 실패 처리
                e.printStackTrace();
                Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                // 요청 성공 시 처리
                if (response.isSuccessful()) {
                    String responseData2 = response.body().string();
                    Log.d("API_RESPONSE", "JSON Response: " + responseData2);


                    // 응답 데이터 파싱
                    try {
                        JSONObject jsonResponse3 = new JSONObject(responseData2);
                        String encData2 = jsonResponse3.getString("enc_data");
                        String data2 = endecryptor2.decrypt(encData2);
                        Log.d("API_RESPONSE", "JSON Response: " + data2);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                } else {
                    // 서버에서 오류 응답이 온 경우 처리
                    // response.code() 및 response.message()를 통해 상세한 정보를 얻을 수 있음
                }
            }
        });

    }

    public void mydata_auth(){
        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken  = sharedPreferences.getString("accesstoken",null);
        SharedPreferences sharedPreferences1 = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url  = sharedPreferences1.getString("apiurl",null);
        String endpoint = "/api/Mydata/mydata_sms_check";
        final String finalUrl = url+endpoint;
        EditText ed = findViewById(R.id.editTextNumber);     // 수취계좌
        //EditText ed1 = findViewById(R.id.edamt);    // 이체금액
        //int to_account = 0;
        int authnum = 0;
        //String sendtime = dateFormat.format(currentDate);

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {

            // fetch values
            if (ed.getText().toString() != "") {
                authnum = Integer.parseInt(ed.getText().toString());
                //amount = Integer.parseInt(ed1.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "Invalid Input ", Toast.LENGTH_SHORT).show();
                onRestart();
            }
            //input your API parameters
            requestDataEncrypted.put("authnum", authnum);
            requestDataEncrypted.put("username","username");

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
                    Intent intent = new Intent(this, Mydata_send.class);
                    startActivity(intent);
                }, error -> Toast.makeText(getApplicationContext(), "Something went wrong[Send]", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers=new HashMap();
                headers.put("Authorization","Bearer "+retrivedToken);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}