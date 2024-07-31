package com.app.turtlebank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Mypage extends AppCompatActivity {

    public void resetPassword(View view){
        // "Back" 버튼을 클릭하면 호출되는 메서드
        // MainActivity로 이동
        Intent into =new Intent(Mypage.this, ResetPassword.class);
        startActivity(into);
        finish();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        final TextView tv_date = findViewById(R.id.text_view_date_main);
        final TextView tv_total=findViewById(R.id.text_view_total_money);
        final TextView tv_username=findViewById(R.id.text_view_name);
        final TextView tv_mem_username=findViewById(R.id.tv_membership_username);
        final CardView cv_mem = findViewById(R.id.cv_membership);
        final TextView tv_mem_s=findViewById(R.id.tv_membership_small);
        final TextView tv_mem_b=findViewById(R.id.tv_membership_big);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken  = sharedPreferences.getString("accesstoken",null);
        final RequestQueue queue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url  = sharedPreferences.getString("apiurl",null);
        String endpoint="/api/balance/total";
        String finalurl = url+endpoint;

        // Enter the correct url for your api service site
        final int initialTimeoutMs = 2000; // 초기 타임아웃 값 (5초)
        final int maxNumRetries = 1; // 최대 재시도 횟수
        final float backoffMultiplier = 1f; // 재시도 간격의 배수

        RetryPolicy policy = new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier);


        final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, finalurl,null,
                new Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
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

                            tv_total.setText(formattedBalance);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
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

            @Override
            public RetryPolicy getRetryPolicy() {
                // RetryPolicy 설정
                return policy;
            }
        };

        endpoint="/api/user/profile";
        finalurl = url+endpoint;
        final JsonObjectRequest stringRequest2 = new JsonObjectRequest(Request.Method.POST, finalurl,null,
                new Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }

                            JSONObject obj = decryptedResponse.getJSONObject("data");
                            String username = obj.getString("username");
                            String membership = obj.getString("membership");

                            if(membership.equals("SILVER")){
                                cv_mem.setCardBackgroundColor(getResources().getColor(R.color.silver));
                            } else if (membership.equals("GOLD")) {
                                cv_mem.setCardBackgroundColor(getResources().getColor(R.color.gold));
                            } else {
                                cv_mem.setCardBackgroundColor(getResources().getColor(R.color.platinum));
                            }
                            tv_mem_s.setText(membership);
                            tv_mem_b.setText(membership);
                            tv_username.setText("HELLO  "+username);
                            tv_mem_username.setText(username+" 님은 ");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            Date currentTime = Calendar.getInstance().getTime();
                            tv_date.setText(format.format(currentTime));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
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
            @Override
            public RetryPolicy getRetryPolicy() {
                // RetryPolicy 설정
                return policy;
            }


        };
        queue.add(stringRequest);
        queue.getCache().clear();
        queue.add(stringRequest2);

        queue.getCache().clear();
    }

}