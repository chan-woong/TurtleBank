package com.app.turtlebank;
/* 뱅크 로그인 화면을 나타내는 "BankLogin" 액티비티를 정의.
안드로이드 앱에서 로그인 인증을 처리하는 과정
사용자가 로그인 정보를 입력하고 서버로 전송하여 인증을 수행하며, 그에 따른 응답을 처리
*/
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BankLogin extends AppCompatActivity {
    public static String loginID;
    private ProgressBar spinner;
    private RelativeLayout priv;
    public void backToMain(View view){
        // "Back" 버튼을 클릭하면 호출되는 메서드
        // MainActivity로 이동
        Intent into =new Intent(BankLogin.this, MainActivity.class);
        startActivity(into);
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        BankLogin.super.onBackPressed();
                        System.exit(0);
                    }
                }).create().show();
    }

    public void bankLogin(View view)
    {
        // "Login" 버튼을 클릭하면 호출되는 메서드
        final TextView t1 = findViewById(R.id.log);
        // 입력된 이메일과 비밀번호를 가져옴
        EditText inputID = findViewById(R.id.loginID_editText);
        EditText inputPassword = findViewById(R.id.login_password_editText);
        priv=(RelativeLayout)findViewById(R.id.relp);
        spinner = (ProgressBar)findViewById(R.id.progressb);
        priv.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        final String id = inputID.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        final String hPassword = hashPassword(password);


        // SharedPreferences를 사용하여 저장된 API URL을 가져옴
        SharedPreferences sharedPreferences = getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl",null);
        String endpoint = "/api/user/login";
        String finalurl = url + endpoint;

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        // HTTP POST 요청을 보내기 위한 데이터를 JSON 형식으로 생성
        JSONObject requestData = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {
            // Input your API parameters
            requestData.put("username", id);
            requestData.put("password",hPassword);

            // Encrypt data before sending
            // 데이터를 암호화
            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestData.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 서버로부터의 응답을 처리
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                // 응답이 성공이면 토스트 메시지를 표시
                                Toast.makeText(getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(BankLogin.this, BankLogin.class));
                                finish();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }

                            loginID = id;
                            JSONObject obj = decryptedResponse.getJSONObject("data");
                            String accessToken=obj.getString("accessToken");
                            SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
                            Log.d("accesstoken",accessToken);
                            sharedPreferences.edit().putString("accesstoken",accessToken).apply();
//                            sharedPreferences.edit().putBoolean("isloggedin",true).apply();
                            // 대시보드 화면(Dashboard)으로 이동
                            startActivity(new Intent(BankLogin.this, BankMainActivity.class));
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 오류 응답일 경우 "Something went wrong" 메시지를 표시
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                // 로그인 화면으로 다시 이동
                startActivity(new Intent(BankLogin.this, BankLogin.class));
                finish();
            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 액티비티가 생성될 때 호출되는 메서드
        super.onCreate(savedInstanceState);
        // activity_login.xml을 화면에 표시
        setContentView(R.layout.activity_login);

    }

    protected static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}