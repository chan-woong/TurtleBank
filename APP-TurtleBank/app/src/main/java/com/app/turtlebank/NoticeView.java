package com.app.turtlebank;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

public class NoticeView extends AppCompatActivity {
    private String selectedId;
    private TextView tv_title;
    private TextView tv_userId;
    private TextView tv_createdAt;
    private TextView tv_updatedAt;
    private TextView tv_filename;
    private TextView tv_content;
    Button edit;
    Button delete;
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
        setContentView(R.layout.activity_noticeview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_title = findViewById(R.id.nv_title);
        tv_userId = findViewById(R.id.nv_userId);
        tv_createdAt = findViewById(R.id.nv_createdAt);
        tv_updatedAt = findViewById(R.id.nv_updatedAt);
        tv_filename = findViewById(R.id.nv_filenames);
        tv_content = findViewById(R.id.nv_content);

        // NoticeListView로부터 넘겨받은 id를 받아옵니다.
        Intent intent = getIntent();
        selectedId = intent.getStringExtra("selectedId");
        admcheck();
        viewNoticeDetails(selectedId);
    }

    public void fileDownload(View view) {
        String filename = tv_filename.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/notice/download";
        String finalUrl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            requestData.put("filename", filename);
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

                            String filepath = itemObject.optString("filepath", "");
                            String downloadFilepath = url + "/api/notice/download?filename=" + filename;

                            // 파일 다운로드를 시작합니다.
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadFilepath));
                            request.setDescription("Downloading file...");
                            request.setTitle("Download");

                            // 다운로드 폴더 설정
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                            // 다운로드가 완료되었을 때 실행할 코드
                            BroadcastReceiver onComplete = new BroadcastReceiver() {
                                public void onReceive(Context ctxt, Intent intent) {
                                    Toast.makeText(ctxt, "Download Complete", Toast.LENGTH_LONG).show();
                                }
                            };

                            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                            // 다운로드를 실행합니다.
                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            dm.enqueue(request);

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

    public void admcheck() {
        if (BankLogin.loginID == null) {
            // loginID가 null인 경우에 대한 처리
            // 예를 들어, Toast 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            Toast.makeText(getApplicationContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return; // 메서드를 더 이상 진행하지 않고 종료합니다.
        }
        assert BankLogin.loginID != null : "BankLogin.loginID is null!";
        username = BankLogin.loginID.trim();
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
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

                            edit = findViewById(R.id.notice_edit_btn);
                            delete = findViewById(R.id.notice_delete_btn);

                            if ("true".equals(isAdmin)) {
                                edit.setVisibility(View.VISIBLE);
                                delete.setVisibility(View.VISIBLE);
                            } else {
                                edit.setVisibility(View.INVISIBLE);
                                delete.setVisibility(View.INVISIBLE);
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
    public void viewNoticeDetails(String id) {

        String rqId = id.trim();

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/notice/getboard";
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
                            String createdAt = itemObject.optString("createdAt", "");
                            String updatedAt = itemObject.optString("updatedAt", "");
                            String filename = itemObject.optString("filepath", "");
                            filename = filename.replace("public/images/", ""); // "public/images/" 부분을 제거합니다
                            String contents = itemObject.optString("content", "");

                            Date createdAtDate = inputFormat.parse(createdAt);
                            Date updatedAtDate = inputFormat.parse(updatedAt);

                            String formattedCreatedAt = outputDateFormat.format(createdAtDate);
                            String formattedUpdatedAt = outputDateFormat.format(updatedAtDate);

                            // 값을 TextView에 설정
                            tv_title.setText(title);
                            tv_userId.setText(userId);
                            tv_createdAt.setText(formattedCreatedAt);
                            tv_updatedAt.setText(formattedUpdatedAt);
                            if (filename != null && !filename.equals("null")) {
                                tv_filename.setText(filename);
                            } else {
                                tv_filename.setText("");
                            }
                            tv_content.setText(contents);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
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

    public void editNotice(View view) {
        // 수정 버튼을 눌렀을 때의 처리입니다.

        // 해당 게시글의 정보를 가져옵니다.
        String id = selectedId;
        String title = tv_title.getText().toString();
        String contents = tv_content.getText().toString();
        String createdAt = tv_createdAt.getText().toString();
        // NoticeWrite 액티비티로 이동합니다.
        Intent intent = new Intent(NoticeView.this, NoticeWrite.class);
        intent.putExtra("from", "noticeview"); // 뷰에서 왔음을 나타내는 플래그
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("content", contents);
        intent.putExtra("createdAt", createdAt);
        startActivity(intent);
        onRestart(); // 현재 액티비티를 종료합니다.
    }

    public void deleteNotice(View view) {
        String id = selectedId;

        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/notice/deleteboard";
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
    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        selectedId = intent.getStringExtra("selectedId");
        viewNoticeDetails(selectedId);
    }
}
