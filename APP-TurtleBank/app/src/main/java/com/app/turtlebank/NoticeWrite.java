package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class NoticeWrite extends AppCompatActivity {

    private String id;
    private String title;
    private String contents;
    private String filename;
    private Uri filepath;
    private String retrivedToken;
    private String original = "no_file_name";
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
        setContentView(R.layout.activity_noticewrite);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // 툴바에 뒤로가기 버튼 표시
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String from = intent.getStringExtra("from");

        if (from.equals("noticelistview")) {
            Toast.makeText(this, "글을 작성해주세요", Toast.LENGTH_SHORT).show();

        } else if (from.equals("noticeview")) {
            // NoticeView에서 왔을 경우
            // 수정하기에서 넘어온 정보를 가져옵니다.
            title = intent.getStringExtra("title");
            contents = intent.getStringExtra("content");
            filename = intent.getStringExtra("filename");
            // 가져온 정보를 활용하여 UI를 초기화합니다.
            EditText titleEditText = findViewById(R.id.ntw_title);
            EditText contentEditText = findViewById(R.id.ntw_content);
            TextView filenameTextView = findViewById(R.id.ntw_filename);
            titleEditText.setText(title);
            contentEditText.setText(contents);
            filenameTextView.setText(filename);

            if (!filenameTextView.getText().toString().isEmpty()) {
                original = filename;
            }
            Log.e("filename", original);

        }
    }

    // 글 작성 완료후 버튼을 눌렀을 때
    public void writeFinish(View view) {
        Intent intent = getIntent();
        String from = intent.getStringExtra("from");
        id = intent.getStringExtra("id");

        EditText titleEditText = findViewById(R.id.ntw_title);
        EditText contentEditText = findViewById(R.id.ntw_content);
        TextView filenameTextview = findViewById(R.id.ntw_filename);

        String title = titleEditText.getText().toString();
        String contents = contentEditText.getText().toString();
        String filename = filenameTextview.getText().toString();

        if (title.isEmpty() || contents.isEmpty()) {
            // 제목 또는 내용이 비어있을 경우 Toast 메시지를 표시합니다.
            Toast.makeText(this, "제목과 내용을 써주세요", Toast.LENGTH_SHORT).show();
            return;  // 메서드 실행을 중단합니다.
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());

        if (from != null && from.equals("noticelistview")) {
            if (!filenameTextview.getText().toString().isEmpty()) {
                uploadFile(filepath);
            }
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl", null);
            String endpoint = "/api/notice/writeboard";
            String finalUrl = url + endpoint;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();

            try {
                if (filenameTextview.getText().toString().equals("")) {
                    requestData.put("title", title);
                    requestData.put("content", contents);
                    requestData.put("createdAt", currentDateAndTime);
                    requestData.put("updatedAt", currentDateAndTime);
                } else {
                    requestData.put("title", title);
                    requestData.put("content", contents);
                    requestData.put("filepath", filename);
                    requestData.put("createdAt", currentDateAndTime);
                    requestData.put("updatedAt", currentDateAndTime);
                }
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

        } else if (from != null && from.equals("noticeview")) {
            if (!filenameTextview.getText().toString().isEmpty()) {
                if (original != null && !original.equals(filename)) {
                    uploadFile(filepath);
                }
            }
            SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
            final String url = sharedPreferences.getString("apiurl", null);
            String endpoint = "/api/notice/editboard";
            String finalUrl = url + endpoint;

            final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject requestData = new JSONObject();
            JSONObject requestDataEncrypted = new JSONObject();

            try {
                if (filenameTextview.getText().toString().equals("")) {
                    requestData.put("id", id);
                    requestData.put("title", title);
                    requestData.put("content", contents);
                    requestData.put("updatedAt", currentDateAndTime);
                } else {
                    requestData.put("id", id);
                    requestData.put("title", title);
                    requestData.put("filepath", filename);
                    requestData.put("content", contents);
                    requestData.put("updatedAt", currentDateAndTime);
                }
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
                    // 오류가 발생했을 때의 처리입니다.
                    // 예: 네트워크 오류 등에 대한 메시지 출력 등
                }
            });

            requestQueue.add(jsonObjectRequest);
            requestQueue.getCache().clear();

        }
    }


    // 파일을 업로드하는 함수
    public void uploadFile(Uri filePath) {
        if (filePath == null) {
            // filePath가 null인 경우 처리할 내용을 여기에 추가
            Toast.makeText(this, "filePath is null", Toast.LENGTH_SHORT).show();
            return;
        }
        // 파일 업로드
        SharedPreferences sharedPreferences = getSharedPreferences("jwt", MODE_PRIVATE);
        retrivedToken = sharedPreferences.getString("accesstoken", null);
        sharedPreferences = getSharedPreferences("apiurl", MODE_PRIVATE);
        String url = sharedPreferences.getString("apiurl", null);

        String endpoint = "/api/notice/mobileupload";
        String finalurl = url + endpoint;
        String fileName = getFileName(filePath);
        Log.e("upload", finalurl);
        Log.e("upload", fileName);

        try {
            byte[] fileData = getFileData(filePath);
            // 여기에 HTTP Request Body 제작하기
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse(getContentResolver().getType(filePath)), fileData))
                    .build();

            okhttp3.Request request= new okhttp3.Request.Builder()
                    .url(finalurl)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("upload", "http 통신 실패");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // 서버 응답이 성공적인 경우 처리할 내용을 여기에 추가
                        Log.e("upload", "http 통신 성공");
                    } else {
                        // 서버 응답이 실패한 경우 처리할 내용을 여기에 추가
                        Log.e("upload", "서버 응답 실패: " + response.code());
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 뒤로가기
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // 첨부파일 버튼을 눌렀을 때
    public void fileUpload(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    // 첨부파일 버튼을 누르면 모바일 기기의 폴더가 열린다. 파일을 선택하면 다시 NoticeWrite로 돌아온다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    filepath = uri; // uri에서 실제 파일 경로를 얻어옵니다.
                    filename = getFileName(uri); // 파일 이름을 얻어옵니다.
                    TextView filenameTextview = findViewById(R.id.ntw_filename);
                    filenameTextview.setText(filename);
                }
            }
        }
    }

    // Uri에서 실제 파일 경로를 얻어오는 메서드
    private String getRealPathFromURI(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(idx);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    // Uri에서 파일 이름을 얻어오는 메서드
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // 파일 변환 [버퍼로 변환]
    private byte[] getFileData(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();
        return byteBuffer.toByteArray();
    }
}