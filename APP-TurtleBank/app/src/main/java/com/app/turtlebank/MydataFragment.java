package com.app.turtlebank;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MydataFragment extends Fragment {

    private String phoneNumber;
    public String userName;
    private JSONArray dataArray;
    private String user_name;

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_mydata, container, false);
        //mydata_sms_auth = getView().findViewById(R.id.mydata_sms_auth);
        Button mydata_sms_auth = rootView.findViewById(R.id.mydata_sms_auth);
        OkHttpClient client2 = new OkHttpClient();
        EncryptDecrypt endecryptor2 = new EncryptDecrypt();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken2  = sharedPreferences.getString("accesstoken",null);

        SharedPreferences mysharedPreferences = getActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", ""); // 저장된 사용자 이름이 없을 경우 빈 문자열을 반환합니다.

        // XML 레이아웃에서 TextView를 찾아 사용자 이름을 설정합니다.
        TextView textViewPortfolio = rootView.findViewById(R.id.text_view_portfolio);
        textViewPortfolio.setText(username + "님,\n흩어져있는 내 자산 연결하고 \n한번에 관리하세요");

        String apiUrl2 = "http://m.turtle-bank.com/api/Account/view";

        RequestBody requestBody2 = new FormBody.Builder()
                .add("username", "username")

                // 다른 필요한 데이터도 추가해주세요
                .build();
        okhttp3.Request request2 = new okhttp3.Request.Builder()
                .url(apiUrl2)
                .post(requestBody2)
                .addHeader("Authorization", "1 " + retrivedToken2)
                .build();
        String encryptedData2 = endecryptor2.encrypt(request2.toString());
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

                        JSONObject dataObject2 = new JSONObject(data2);
                        dataArray = dataObject2.getJSONArray("data");
                        JSONObject firstObject = dataArray.getJSONObject(0);
                        String username = firstObject.getString("username");
                        Log.d("API_RESPONSE", "user 뽑아오기: " + username);

                        // SharedPreferences에 사용자 이름 저장
                        SharedPreferences mysharedPreferences = getActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mysharedPreferences.edit();
                        editor.putString("username", username); // "username" 키로 사용자 이름 저장
                        editor.apply(); // 변경 사항 저장

                        TextView textViewPortfolio = getView().findViewById(R.id.text_view_portfolio);
                        textViewPortfolio.setText(username + "님,\n흩어져있는 내 자산 연결하고 \n한번에 관리하세요");


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                } else {
                    // 서버에서 오류 응답이 온 경우 처리
                    // response.code() 및 response.message()를 통해 상세한 정보를 얻을 수 있음
                }
            }
        });

        mydata_sms_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "linear_layout_send_money" 버튼 클릭 시 실행할 코드

                // Intent를 통해 새로운 Activity 시작
                //Intent intent = new Intent(this, Mydata_auth.class);
                reqAccounts();
                Intent intent = new Intent(getActivity(), Mydata_auth.class);
                startActivity(intent);
                startActivityForResult(intent, 1);
            }
        });
        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // 1은 startActivityForResult()에서 사용한 요청 코드입니다.
            if (resultCode == Activity.RESULT_OK) {
                // 활동이 성공적으로 반환된 경우
                Intent intent = new Intent(getActivity(), Mydata_send.class);
                startActivity(intent);
                //reqAccounts();
            }
        }
    }

    private void reqAccounts()
    {
        OkHttpClient client2 = new OkHttpClient();
        EncryptDecrypt endecryptor2 = new EncryptDecrypt();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken2  = sharedPreferences.getString("accesstoken",null);

        String apiUrl2 = "http://m.turtle-bank.com/api/Mydata/req_account";

        RequestBody requestBody2 = new FormBody.Builder()
                .add("username", "username")
                .add("authnum","authnum")
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

//                        JSONObject dataObject2 = new JSONObject(data2);
//                        dataArray = dataObject2.getJSONArray("data");
//                        JSONObject firstObject = dataArray.getJSONObject(0);
//                        String username = firstObject.getString("username");
//                        Log.d("API_RESPONSE", "user 뽑아오기: " + username);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("API_RESPONSE", "Error: " + e.getMessage());
                    }
                } else {
                    // 서버에서 오류 응답이 온 경우 처리
                    // response.code() 및 response.message()를 통해 상세한 정보를 얻을 수 있음
                }
            }
        });
    }

}