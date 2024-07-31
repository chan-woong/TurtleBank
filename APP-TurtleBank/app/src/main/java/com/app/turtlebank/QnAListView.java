package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

public class QnAListView extends AppCompatActivity implements Qadapter.OnItemClickListener {

    RecyclerView recyclerView;
    List<QnAListRecords> qRecords;
    Qadapter qadapter;
    EditText searchInput;
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
        setContentView(R.layout.activity_qna_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.qnaRlist);
        qRecords = new ArrayList<>();
        viewQnAList();

    }

    public void qna_searchBtn(View view) {
        qRecords.clear();
        EditText searchEditText = findViewById(R.id.qna_search);
        String searchtext = searchEditText.getText().toString().trim();

        if (searchtext == "") {
            viewQnAList();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl",null);
            String endpoint = "/api/qna/searchboard";
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

                                    QnAListRecords item = new QnAListRecords(id, userId, title, updatedAt);
                                    qRecords.add(item);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            qadapter = new Qadapter(getApplicationContext(), qRecords);
                            recyclerView.setAdapter(qadapter);

                            Integer count = qadapter.getItemCount();
                            if (count == 0) {
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            qadapter.setOnItemClickListener(QnAListView.this);

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

    public void viewQnAList() {
        qRecords.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/qna/viewboard";
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

                                QnAListRecords item = new QnAListRecords(id, userId, title, updatedAt);
                                qRecords.add(item);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        qadapter = new Qadapter(getApplicationContext(), qRecords);
                        recyclerView.setAdapter(qadapter);


                        Integer count = qadapter.getItemCount();
                        if (count == 0) {
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        qadapter.setOnItemClickListener(QnAListView.this);

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

    public void goToQnaWrite(View view) {
        Intent intent = new Intent(this, QnAWrite.class);
        intent.putExtra("froms", "qnalistview"); // 리스트뷰에서 왔음을 나타내는 플래그
        startActivity(intent);
        onRestart();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, QnAView.class);

        String selectedId = qRecords.get(position).getId();
        intent.putExtra("selectedId", selectedId);
        Log.e("sendId", selectedId);
        startActivity(intent);
        onRestart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        viewQnAList(); // 최신 데이터를 가져오는 메서드 호출
    }

}
