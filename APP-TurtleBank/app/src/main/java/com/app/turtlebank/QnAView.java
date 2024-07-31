package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QnAView extends AppCompatActivity {

    private String selectedId;
    private TextView tv_title;
    private TextView tv_userId;
    private TextView tv_createdAt;
    private TextView tv_updatedAt;
    private TextView tv_content;
    private TextView tv_comment;
    Button edit;
    Button delete;
    Button comment;
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
        setContentView(R.layout.activity_qna_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tv_title = findViewById(R.id.qv_title);
        tv_userId = findViewById(R.id.qv_userId);
        tv_createdAt = findViewById(R.id.qv_createdAt);
        tv_updatedAt = findViewById(R.id.qv_updatedAt);
        tv_content = findViewById(R.id.qv_content);
        tv_comment = findViewById(R.id.qv_comment);

        // QnAListView로부터 넘겨받은 id를 받아옵니다.
        Intent intent = getIntent();
        selectedId = intent.getStringExtra("selectedId");
        Log.e("id", selectedId);
        viewQnADetails(selectedId);
        checkAdmUsr();
    }
    
    public void checkAdmUsr() {
        // 관리자 or 본인이 쓴 글인지 확인한다.
        Intent intent = getIntent();
        selectedId = intent.getStringExtra("selectedId").trim();
        username = BankLogin.loginID.trim();

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/qna/admusrcheck";
        String finalUrl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            requestData.put("id", selectedId);
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
                            String checkUser = itemObject.optString("check_user", "");
                            Log.d("is_admin", isAdmin);
                            Log.d("check_user", checkUser);

                            edit = findViewById(R.id.qna_edit_btn);
                            delete = findViewById(R.id.qna_delete_btn);
                            comment = findViewById(R.id.qna_comment_btn);

                            if (isAdmin.equals("true") || checkUser == "true") {
                                edit.setVisibility(View.VISIBLE);
                                delete.setVisibility(View.VISIBLE);
                            } else {
                                edit.setVisibility(View.INVISIBLE);
                                delete.setVisibility(View.INVISIBLE);
                            }
                            if (isAdmin.equals("true")) {
                                comment.setVisibility(View.VISIBLE);
                            } else {
                                comment.setVisibility(View.INVISIBLE);
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
    
    

    public void viewQnADetails(String id) {
        Log.d("QnAView", "viewQnADetails 시작");
        String rqId = id.trim();

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/qna/getboard";
        String finalUrl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            //input your API parameters
            requestData.put("id", rqId);
            // Encrypt data before sending
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

                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

                            JSONObject itemObject = decryptedResponse.getJSONObject("data");

                            String id = itemObject.optString("id", "");
                            String userId = itemObject.optString("userId", "");
                            String title = itemObject.optString("title", "");
                            String contents = itemObject.optString("content", "");
                            String createdAt = itemObject.optString("createdAt", "");
                            String updatedAt = itemObject.optString("updatedAt", "");
                            String comment = itemObject.optString("comment", "");

                            Date createdAtDate = inputFormat.parse(createdAt);
                            Date updatedAtDate = inputFormat.parse(updatedAt);

                            String formattedCreatedAt = outputDateFormat.format(createdAtDate);
                            String formattedUpdatedAt = outputDateFormat.format(updatedAtDate);

                            // 값을 TextView에 설정
                            tv_title.setText(title);
                            tv_userId.setText(userId);
                            tv_createdAt.setText(formattedCreatedAt);
                            tv_updatedAt.setText(formattedUpdatedAt);
                            tv_content.setText(contents);
                            tv_comment.setText(comment);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "JSON 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (ParseException e) {
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

    public void editQnA(View view) {
        // 수정 버튼을 눌렀을 때의 처리입니다.

        // 해당 게시글의 정보를 가져옵니다.
        String id = selectedId;
        String title = tv_title.getText().toString();
        String contents = tv_content.getText().toString();
        String createdAt = tv_createdAt.getText().toString();
        String comment = tv_comment.getText().toString();
        // QnAWrite 액티비티로 이동합니다.
        Intent intent = new Intent(QnAView.this, QnAWrite.class);
        intent.putExtra("froms", "qnaview"); // 뷰에서 왔음을 나타내는 플래그
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("content", contents);
        intent.putExtra("createdAt", createdAt);
        intent.putExtra("comment", comment);
        startActivity(intent);
        onRestart();
    }

    public void deleteQnA(View view) {
        String id = selectedId;

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/qna/deleteboard";
        String finalUrl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            // input your API parameters
            requestData.put("id", id);
            // Encrypt data before sending
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

                            Toast.makeText(getApplicationContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                            // 삭제 완료 후 QnAListView로 이동
                            Intent intent = new Intent(QnAView.this, QnAListView.class);
                            startActivity(intent);
                            finish(); // 현재 액티비티를 종료합니다.

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
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


    public void commentQnA(View view) {
        String id = selectedId;
        String title = tv_title.getText().toString();
        String contents = tv_content.getText().toString();
        String createdAt = tv_createdAt.getText().toString();
        String comment = tv_comment.getText().toString();
        // QnAWrite 액티비티로 이동합니다.
        Intent intent = new Intent(QnAView.this, QnAWrite.class);
        intent.putExtra("froms", "admin_comment"); // 뷰에서 왔음을 나타내는 플래그
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("content", contents);
        intent.putExtra("createdAt", createdAt);
        intent.putExtra("comment", comment);
        startActivity(intent);
        onRestart();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        selectedId = intent.getStringExtra("selectedId");
        viewQnADetails(selectedId);
    }

    public String getFinalUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/qna/getboard";
        return url + endpoint;
    }

}
