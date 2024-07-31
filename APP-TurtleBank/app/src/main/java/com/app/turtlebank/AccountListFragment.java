package com.app.turtlebank;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;

public class AccountListFragment extends Fragment {
    private static final int REQUEST_TRANSFER = 123;
    private JSONArray dataArray;

    LinearLayout linear_layout_send_money;
    ImageView add_bank_account;
    RecyclerView recyclerViewbankaccount;
    TextView text_view_name, date,text_view_total_money, text_view_code;
    Button send_btn, show_QR, pick_QR;



    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        try {
            // 5초 동안 슬립
            Thread.sleep(300); // 밀리초 단위로 대기 시간을 지정합니다.
        } catch (InterruptedException e) {
            // 스레드가 interrupted 될 때 발생하는 예외를 처리합니다.
            e.printStackTrace();
        }
        total();
        fetchAccountData();
        // 레이아웃만 인플레이트하고 실제 데이터는 onViewCreated() 메서드에서 처리
        return inflater.inflate(R.layout.fragment_account_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // 5초 동안 슬립
            Thread.sleep(300); // 밀리초 단위로 대기 시간을 지정합니다.
        } catch (InterruptedException e) {
            // 스레드가 interrupted 될 때 발생하는 예외를 처리합니다.
            e.printStackTrace();
        }
        // 데이터 가져오기 및 UI 업데이트
        fetchAccountData();
        total();
        username();
        // 나머지 작업 수행
        define();
        setDate();
        click();
    }


    public void total(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken  = sharedPreferences.getString("accesstoken",null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url  = sharedPreferences.getString("apiurl",null);
        String endpoint="/api/balance/total";
        String finalurl = url+endpoint;

        final JsonObjectRequest stringRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, finalurl,null,
                new com.android.volley.Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity().getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }

                            JSONArray dataArray = decryptedResponse.getJSONArray("data");
                            JSONObject obj = dataArray.getJSONObject(0);
                            String balance=obj.getString("total_balance");

                            // balance를 숫자로 변환
                            double balanceValue = Double.parseDouble(balance);
                            // 숫자를 1000단위로 쉼표로 나누기 위한 포맷 지정
                            DecimalFormat formatter = new DecimalFormat("#,###");
                            // 포맷을 적용하여 문자열로 변환
                            String formattedBalance = formatter.format(balanceValue);

                            try {
                                Thread.sleep(300); // 1000 밀리초 = 1초
                                text_view_total_money.setText(formattedBalance);
                                // 1초 후에 실행될 코드 작성
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("sss", String.valueOf(error));
            }
        }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers=new HashMap();
                headers.put("Authorization","Bearer "+retrivedToken);
                return headers;
            }


        };

        queue.add(stringRequest);
        queue.getCache().clear();
    }

    public void username(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken  = sharedPreferences.getString("accesstoken",null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url  = sharedPreferences.getString("apiurl",null);
        String endpoint="/api/user/profile";
        String finalurl = url+endpoint;
        final JsonObjectRequest stringRequest2 = new JsonObjectRequest(com.android.volley.Request.Method.POST, finalurl,null,
                new com.android.volley.Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("ddss", String.valueOf(response));
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity().getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }

                            JSONObject obj = decryptedResponse.getJSONObject("data");
                            String username = obj.getString("username");

                            text_view_name.setText("HELLO  "+username);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers=new HashMap();
                headers.put("Authorization","Bearer "+retrivedToken);
                return headers;
            }
        };
        queue.add(stringRequest2);
        queue.getCache().clear();
    }

    private void updateRecyclerView(JSONArray dataArray) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("API_RESPONSE", "JSON Response2222222222222: " + dataArray);
                ArrayList<BankAccount> bankAccounts = convertJSONArrayToArrayList(dataArray);

                recyclerViewbankaccount.setHasFixedSize(true);
                recyclerViewbankaccount.setLayoutManager(new LinearLayoutManager(getActivity()));

                MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(bankAccounts, getActivity());
                recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
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
                        bankAccount.setBank_code("터틀뱅크");
                    }
                    else if(jsonObject.getInt("bank_code")==333)
                        bankAccount.setBank_code("래빗뱅크");
                    bankAccounts.add(bankAccount);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("API_RESPONSE", "arrayerrorrrrrrrrrrrrrrrrr: " + e.getMessage());
                }
            }
        }

        return bankAccounts;
    }


    private void fetchAccountData() {
        OkHttpClient client = new OkHttpClient();
        EncryptDecrypt endecryptor = new EncryptDecrypt();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);

        String apiUrl = "http://m.turtle-bank.com/api/Account/view";

        RequestBody requestBody = new FormBody.Builder()
                .build();

        Request request2 = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("Authorization", "1 " + retrivedToken)
                .build();


        // 비동기적으로 API 요청 보내기
        client.newCall(request2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 요청 실패 처리
                e.printStackTrace();
                Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 요청 성공 시 처리
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // 응답 데이터 파싱
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String encData = jsonResponse.getString("enc_data");
                        String data = endecryptor.decrypt(encData);

                        JSONObject dataObject = new JSONObject(data);

                        dataArray = dataObject.getJSONArray("data");

                        updateRecyclerView(dataArray);

                        // TODO: 가져온 값들을 사용하여 원하는 작업 수행
                        // 예를 들면 UI 업데이트 등
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void define(){
        text_view_name = getView().findViewById(R.id.text_view_name);
        date = getView().findViewById(R.id.text_view_date_main);
        recyclerViewbankaccount = getView().findViewById(R.id.recyclerview_bank_account);
        add_bank_account = getView().findViewById(R.id.image_view_add_bank_account);
        text_view_total_money = getView().findViewById(R.id.text_view_total_money);
        linear_layout_send_money = getView().findViewById(R.id.linear_layout_send_money);
        send_btn = getView().findViewById(R.id.send_btn);
        text_view_code = getView().findViewById(R.id.text_view_bank_code);


        show_QR = getView().findViewById(R.id.show_QR);
        pick_QR = getView().findViewById(R.id.pick_QR);
    }


    public void click() {
        // "ADD" 버튼 클릭 이벤트 처리 코드
        add_bank_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 버튼을 눌렀을 때 실행되는 코드

                // OkHttp 클라이언트 생성
                OkHttpClient client = new OkHttpClient();
                EncryptDecrypt endecryptor = new EncryptDecrypt();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
                final String retrivedToken  = sharedPreferences.getString("accesstoken",null);

                // API 엔드포인트 URL 설정
                String apiUrl = "http://m.turtle-bank.com/api/Account/create";

                // 요청 바디에 필요한 데이터 설정 (예: 사용자 정보, 계좌 정보 등)
                // 아래는 예시일 뿐 실제로는 사용자 입력 등을 통해 값을 동적으로 설정해야 합니다.
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", "username")
                        .add("balance", "balance")
                        .add("account_number", "account_number")
                        .add("bank_code", "bank_code")
                        // 다른 필요한 데이터도 추가해주세요
                        .build();

                // API 요청 생성
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(requestBody)
                        .addHeader("Authorization", "1 " + retrivedToken)
                        .build();

                String encryptedData = endecryptor.encrypt(request.toString());

                // 비동기적으로 API 요청 보내기
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 요청 실패 처리
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 요청 성공 시 처리
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // 응답 데이터 파싱
                            try {
                                JSONObject jsonResponse2 = new JSONObject(responseData);
                                String encData = jsonResponse2.getString("enc_data");
                                String data = endecryptor.decrypt(encData);
                                JSONObject jsonResponse = new JSONObject(data);
                                JSONObject dataObject = jsonResponse.getJSONObject("data");
                                fetchAccountData();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("API_RESPONSE", "Error: " + e.getMessage());
                            }
                        }
                    }
                });
                Toast.makeText(getActivity().getApplicationContext(), "계좌가 생성되었습니다.", Toast.LENGTH_SHORT).show();

                try {
                    Thread.sleep(700); // 1000 밀리초 = 1초
                    total();
                    // 1초 후에 실행될 코드 작성
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "linear_layout_send_money" 버튼 클릭 시 실행할 코드

                // Intent를 통해 새로운 Activity 시작
                Intent intent = new Intent(getActivity(), SendMoney.class);
                startActivityForResult(intent, 1);
            }
        });

        show_QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "linear_layout_send_money" 버튼 클릭 시 실행할 코드

                // Intent를 통해 새로운 Activity 시작
                Intent intent = new Intent(getActivity(), QR_receive_account.class);
                startActivityForResult(intent, 1);
            }
        });

        pick_QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "linear_layout_send_money" 버튼 클릭 시 실행할 코드
                startActivity(new Intent(getActivity(), ScanQR.class));
            }
        });


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // 1은 startActivityForResult()에서 사용한 요청 코드입니다.
            if (resultCode == Activity.RESULT_OK) {
                // 활동이 성공적으로 반환된 경우
                fetchAccountData();
                total();
            }
        }
    }

    public void setDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date currentTime = Calendar.getInstance().getTime();
        date.setText(format.format(currentTime));
    }
    public Date getDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date currentTime = Calendar.getInstance().getTime();
        return currentTime;
    }

}