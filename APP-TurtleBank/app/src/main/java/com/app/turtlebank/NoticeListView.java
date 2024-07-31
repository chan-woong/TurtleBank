package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeListView extends AppCompatActivity implements Nadapter.OnItemClickListener {

    RecyclerView recyclerView;
    List<NoticeListRecords> nRecords;
    Nadapter nadapter;
    EditText searchInput;
    Button write;

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
        setContentView(R.layout.activity_noticelistview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.announcelist);
        nRecords = new ArrayList<>();
        admCheck();
        viewNoticeList();

    }

    public void searchBtn(View view) {
        nRecords.clear();
        EditText searchEditText = findViewById(R.id.notice_search);
        String searchtext = searchEditText.getText().toString().trim();

        if (searchtext == "") {
            viewNoticeList();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl",null);
            String endpoint = "/api/notice/searchboard";
            String finalUrl = url + endpoint;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();

            try {
                requestData.put("searchtext", searchtext);
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
                                    // This is buggy. Need to call Login activity again if incorrect credentials are given
                                }

                                JSONArray jsonArray = decryptedResponse.getJSONArray("data");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject itemObject = jsonArray.getJSONObject(i);

                                    String id = itemObject.getString("id");
                                    String userId = itemObject.getString("userId");
                                    String title = itemObject.getString("title");
                                    String updatedAt = itemObject.getString("updatedAt");

                                    NoticeListRecords item = new NoticeListRecords(id, userId, title, updatedAt);
                                    nRecords.add(item);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            nadapter = new Nadapter(getApplicationContext(), nRecords);
                            recyclerView.setAdapter(nadapter);

                            Integer count = nadapter.getItemCount();
                            if (count == 0) {
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            nadapter.setOnItemClickListener(NoticeListView.this);

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

    }

    public void admCheck() {
        if (BankLogin.loginID == null) {
            // loginID가 null인 경우에 대한 처리
            // 예를 들어, Toast 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            Toast.makeText(getApplicationContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return; // 메서드를 더 이상 진행하지 않고 종료합니다.
        }
        assert BankLogin.loginID != null : "BankLogin.loginID is null!";
        username = BankLogin.loginID.trim();
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/notice/admcheck";
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

                            write = findViewById(R.id.notice_write_btn);
                            if ("true".equals(isAdmin)) {
                                write.setVisibility(View.VISIBLE);
                            } else {
                                write.setVisibility(View.INVISIBLE);
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


    public void viewNoticeList() {
        nRecords.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/notice/viewboard";
        final String finalurl = url + endpoint;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, finalurl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }

                            JSONArray jsonArray = decryptedResponse.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject itemObject = jsonArray.getJSONObject(i);

                                String id = itemObject.getString("id");
                                String userId = itemObject.getString("userId");
                                String title = itemObject.getString("title");
                                String updatedAt = itemObject.getString("updatedAt");

                                NoticeListRecords item = new NoticeListRecords(id, userId, title, updatedAt);
                                nRecords.add(item);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        nadapter = new Nadapter(getApplicationContext(), nRecords);
                        recyclerView.setAdapter(nadapter);

                        Integer count = nadapter.getItemCount();
                        if (count == 0) {
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        nadapter.setOnItemClickListener(NoticeListView.this);

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

        queue.add(jsonArrayRequest);
        queue.getCache().clear();
    }

    public void goToNoticeWrite(View view) {
        // 작성하기 버튼을 눌렀을 때의 처리입니다.
        Intent intent = new Intent(NoticeListView.this, NoticeWrite.class);
        intent.putExtra("from", "noticelistview"); // 리스트뷰에서 왔음을 나타내는 플래그
        startActivity(intent);
        onRestart();
    } // 글쓰기 버튼을 누르면 글쓰기 레이아웃으로 화면 전환

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, NoticeView.class);
        String selectedId = nRecords.get(position).getId();
        intent.putExtra("selectedId", selectedId);
        startActivity(intent);
        onRestart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        viewNoticeList();
    }
}