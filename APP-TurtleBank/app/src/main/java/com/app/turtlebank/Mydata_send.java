package com.app.turtlebank;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Mydata_send extends AppCompatActivity {

    Button send;
    private JSONArray dataArray;
    RecyclerView recyclerViewbankaccount;
    LinearLayout secondLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydata_account_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        define();
        reqAccounts();
        //send = findViewById(R.id.buttonSend);
        //send.setOnClickListener(v -> mydata_send());
    }

    private void reqAccounts()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);



        OkHttpClient client2 = new OkHttpClient();
        EncryptDecrypt endecryptor2 = new EncryptDecrypt();
    //    SharedPreferences sharedPreferences = Mydata_send.this.getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken2  = sharedPreferences.getString("accesstoken",null);

        String apiUrl2 = "http://m.turtle-bank.com/api/Mydata/req_account";

        RequestBody requestBody2 = new FormBody.Builder()

                // 다른 필요한 데이터도 추가해주세요
                .build();

        okhttp3.Request request2 = new okhttp3.Request.Builder()
                .url(apiUrl2)
                .post(requestBody2)
                .addHeader("Authorization", "1 " + retrivedToken2)
                .build();
        String encryptedData2 = endecryptor2.encrypt(request2.toString());

        Log.d("API_RESPONSE", "JSON Response: " + request2);
        Log.d("API_RESPONSE", "JSON 22222222222222: " + requestBody2);
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
                        JSONObject dataObject = new JSONObject(data2);
                        dataArray = dataObject.getJSONArray("data");
                        Log.d("API_RESPONSE", "JSON Response: " + dataArray);

                        updateRecyclerView(dataArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void updateRecyclerView(JSONArray dataArray) {
        Mydata_send.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("API_RESPONSE", "JSON Response2222222222222: " + dataArray);
                ArrayList<BankAccount> bankAccounts = convertJSONArrayToArrayList(dataArray);

                recyclerViewbankaccount.setHasFixedSize(true);
                recyclerViewbankaccount.setLayoutManager(new LinearLayoutManager(Mydata_send.this));

                MydataAccountAdapter mydataAccountAdapter = new MydataAccountAdapter(bankAccounts, Mydata_send.this);
                recyclerViewbankaccount.setAdapter(mydataAccountAdapter);
            }
        });
    }

    private ArrayList<BankAccount> convertJSONArrayToArrayList(JSONArray jsonArray) {
        ArrayList<BankAccount> bankAccounts = new ArrayList<>();
        Log.d("API_RESPONSE", "lengthglafjsafaas: " + jsonArray.length());

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setBalance(jsonObject.getInt("balance"));
                    bankAccount.setAccount_number(jsonObject.getInt("account_number"));
                    if(jsonObject.getInt("bank_code")==555) {
                        bankAccount.setBank_code("TurtleBank");
                    }
                    else if(jsonObject.getInt("bank_code")==333)
                        bankAccount.setBank_code("RabbitBank");
                    bankAccounts.add(bankAccount);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("API_RESPONSE", "arrayerrorrrrrrrrrrrrrrrrr: " + e.getMessage());
                }
            }
        }

        return bankAccounts;
    }

    public void click(@NonNull MydataAccountAdapter.ViewHolder holder, final int position) {
        // "linear_layout_send_money" 버튼 클릭 이벤트 처리 코드

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // Mydata_sendMoney 액티비티의 결과 처리
            if (resultCode == Activity.RESULT_OK) {
                reqAccounts();
            } else {
                // 취소 또는 실패한 결과 처리 코드
            }
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 뒤로가기 버튼이 눌렸을 때의 동작 추가
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void define(){
        recyclerViewbankaccount = Mydata_send.this.findViewById(R.id.recyclerview_bank_account);
    }

}
