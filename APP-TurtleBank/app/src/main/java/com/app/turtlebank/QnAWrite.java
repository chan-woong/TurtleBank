package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QnAWrite extends AppCompatActivity {

    private String id;
    private String title;
    private String contents;
    private String comment;
    LinearLayout layout;
    private String username;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna_write);
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String from = intent.getStringExtra("froms");

        if (from.equals("qnalistview")) {

        } else if (from.equals("qnaview")) {
            title = intent.getStringExtra("title");
            contents = intent.getStringExtra("content");
            comment = intent.getStringExtra("comment");

            // 가져온 정보를 활용하여 UI를 초기화합니다.
            EditText titleEditText = findViewById(R.id.qna_w_title);
            EditText contentEditText = findViewById(R.id.qna_w_content);
            EditText commentEditText = findViewById(R.id.qna_w_comment);
            titleEditText.setText(title);
            contentEditText.setText(contents);
        } else if (from.equals("admin_comment")) {

            title = intent.getStringExtra("title");
            contents = intent.getStringExtra("content");
            comment = intent.getStringExtra("comment");

            // 가져온 정보를 활용하여 UI를 초기화합니다.
            EditText titleEditText = findViewById(R.id.qna_w_title);
            EditText contentEditText = findViewById(R.id.qna_w_content);
            EditText commentEditText = findViewById(R.id.qna_w_comment);
            titleEditText.setText(title);
            contentEditText.setText(contents);
            commentEditText.setText(comment);
            titleEditText.setEnabled(false);
            contentEditText.setEnabled(false);
        }

        checkAdm();

    }

    public void checkAdm() {
        // 관리자만 답변을 쓸 수 있다.
        username = BankLogin.loginID.trim();

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/qna/admcheck";
        String finalUrl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            requestData.put("username", username);
            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject itemObject = decryptedResponse.getJSONObject("data");
                            String isAdmin = itemObject.optString("is_admin", "");
                            Log.d("is_admin", isAdmin);

                            layout = findViewById(R.id.qna_l_comment);

                            if (isAdmin.equals("true")) {
                                layout.setVisibility(View.VISIBLE);
                            } else {
                                layout.setVisibility(View.INVISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Something Response wrong", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
                final String retrivedToken = sharedPreferences.getString("accesstoken", null);
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
        requestQueue.getCache().clear();
    }

    public void qnaWriteFinish(View view) {

        Intent intent = getIntent();
        String from = intent.getStringExtra("froms");
        id = intent.getStringExtra("id");

        EditText titleEditText = findViewById(R.id.qna_w_title);
        EditText contentEditText = findViewById(R.id.qna_w_content);
        EditText commentEditText = findViewById(R.id.qna_w_comment);
        String userId = BankLogin.loginID.trim();
        String title = titleEditText.getText().toString();
        String contents = contentEditText.getText().toString();
        String comment = commentEditText.getText().toString();

        if (title.isEmpty() || contents.isEmpty()) {
            // 제목 또는 내용이 비어있을 경우 Toast 메시지를 표시합니다.
            Toast.makeText(this, "제목과 내용을 써주세요", Toast.LENGTH_SHORT).show();
            return;  // 메서드 실행을 중단합니다.
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());


        if (from.equals("qnalistview")) {
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl",null);
            String endpoint = "/api/qna/writeboard";
            String finalUrl = url + endpoint;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();

            try {
                requestData.put("userId", userId);
                requestData.put("title", title);
                requestData.put("content", contents);
                requestData.put("createdAt", currentDateAndTime);
                requestData.put("updatedAt", currentDateAndTime);
                requestData.put("comment", comment);
                requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, requestDataEncrypted,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                                if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                    Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                finish(); // 현재 액티비티를 종료합니다.

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(jsonObjectRequest);
            requestQueue.getCache().clear();
        } else if (from.equals("qnaview") || from.equals("admin_comment")) {
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl",null);
            String endpoint = "/api/qna/editboard";
            String finalUrl = url + endpoint;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();

            try {
                requestData.put("id", id);
                requestData.put("title", title);
                requestData.put("content", contents);
                requestData.put("updatedAt", currentDateAndTime);
                requestData.put("comment", comment);
                requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, requestDataEncrypted,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                                if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                    Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // 오류가 발생했을 때의 처리입니다.
                    // 예: 네트워크 오류 등에 대한 메시지 출력 등
                }
            });

            requestQueue.add(jsonObjectRequest);
            requestQueue.getCache().clear();
        }
    }




}
