package com.app.turtlebank;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.util.Log;

public class TransactionFragment extends Fragment {
    private ViewGroup rootView;
    private TransactionAdapter adapter;
    private int selectedYear, selectedMonth, selectedDay;
    private String tripstart;
    private String tripend;
    private String seoultime;
    private long now;
    private Date date;

    // 서울시간 현재 시간 구하기

    public void seoulTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        now = System.currentTimeMillis();
        date = new Date(now);
        seoultime = sdf.format(date);
    }


    private void showStartDatePicker(View view) {
        showDatePickerDialog((Button) view, true); //true는 시작일

    }

    private void showEndDatePicker(View view) {
        showDatePickerDialog((Button) view, false); //false는 종료일

    }
    private void showDatePickerDialog(final Button dateButton,final boolean isStartDate) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // 선택된 날짜를 TextView에 표시
                        dateButton.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);

                        if (isStartDate) {
                            tripstart = dateButton.getText().toString();
                        } else {
                            tripend = dateButton.getText().toString();
                        }
                    }
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }


    private void onSearchButtonClick() {

        getTransaction(tripstart+" 00:00:00",tripend+" 23:59:59");
    }

    //234506, 153145
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_transaction, container, false);

        seoulTime();
        //버튼에 대한 뷰를 연결
        Button btnSelectStartDate = rootView.findViewById(R.id.btn_select_start_date);
        Button btnSelectEndDate = rootView.findViewById(R.id.btn_select_end_date);
        Button btnSearchDate = rootView.findViewById(R.id.btn_search_date);

        // 시작일 선택 버튼 클릭 리스너 설정
        btnSelectStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePicker(v);
            }
        });

        // 종료일 선택 버튼 클릭 리스너 설정
        btnSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePicker(v);
            }
        });

        btnSearchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method or perform actions when the search button is clicked
                onSearchButtonClick();
            }
        });

        String init_start_date = "1998-02-20 00:00:00";
        String init_end_date = seoultime;
        getTransaction(init_start_date, init_end_date);
        return rootView;
    }

    @Override
    public void onResume() {
        String init_start_date = "1998-02-20 00:00:00";
        String init_end_date = seoultime;
        super.onResume();
        getTransaction(init_start_date,init_end_date);
    }

    public void getTransaction(String start_date, String end_date) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/transactions/view/search";
        final String finalurl = url + endpoint;

        // JSON 데이터 생성
        JSONObject jsonBody = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {

            jsonBody.put("tripstart", start_date);
            jsonBody.put("tripend", end_date);

            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(jsonBody.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        // 헤더에 JWT 토큰 추가
        SharedPreferences jwtPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = jwtPreferences.getString("accesstoken", null);

        // 요청 큐 생성
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        // POST 요청 생성
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 요청이 성공한 경우 처리
                        Log.d("API Response", response.toString());
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity().getApplicationContext(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                                // This is buggy. Need to call Login activity again if incorrect credentials are given
                            }


                            List<JSONObject> transactionList = new ArrayList<>();
                            try {
                                JSONArray jsonArray = decryptedResponse.getJSONObject("data").getJSONArray("result");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    transactionList.add(jsonArray.getJSONObject(i));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            adapter = new TransactionAdapter(getActivity(), transactionList);
                            RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_transaction);
                            if (recyclerView != null) {
                                // LinearLayoutManager 설정
                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                                // 어댑터 설정
                                recyclerView.setAdapter(adapter);
                            } else {
                                Log.e("RecyclerView Error", "RecyclerView is null");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 요청이 실패한 경우 처리
                Log.e("API Error", "Error during API request", error);
            }
        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                // 헤더에 JWT 토큰 추가
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        // 요청 큐에 추가
        queue.add(jsonArrayRequest);
        queue.getCache().clear(); // 요청이 캐시되는 것을 방지하기 위해 캐시를 클리어합니다.
    }
}