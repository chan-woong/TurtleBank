package com.app.turtlebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    TextView tv_account;
    TextView tv_balance;
    TextView tv_user;
    RequestQueue queue;
    RecyclerView recyclerView_notice;
    RecyclerView recyclerView_qna;
    List<NoticeListRecords> nRecords = new ArrayList<>();
    List<QnAListRecords> qRecords = new ArrayList<>();
    Nadapter nadapter;
    Qadapter qadapter;
    LinearLayout ll_notice;
    LinearLayout ll_qna;
    LinearLayout ll_mydata;
    LinearLayout ll_loan;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup homeView = (ViewGroup)inflater.inflate(R.layout.fragment_home, container, false);

        tv_account = homeView.findViewById(R.id.textview_account_number);
        tv_balance = homeView.findViewById(R.id.textview_balance);
        tv_user    = homeView.findViewById(R.id.tv_home_username);


//        recyclerView_notice = homeView.findViewById(R.id.announcelist); // Assuming the RecyclerView ID is 'recycler_view' in your layout
//        recyclerView_notice.setLayoutManager(new LinearLayoutManager(getActivity()));
//        viewNoticeList();
//
//
//        recyclerView_qna.setLayoutManager(new LinearLayoutManager(getActivity()));
//        viewQnAList();

        ll_notice = homeView.findViewById(R.id.ll_notice);
        ll_qna = homeView.findViewById(R.id.ll_qna);
        ll_mydata = homeView.findViewById(R.id.ll_mydata);
        ll_loan = homeView.findViewById(R.id.ll_loan);
        ll_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), NoticeListView.class);
                startActivity(intent);
            }
        });

        ll_qna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), QnAListView.class);
                startActivity(intent);
            }
        });

        ll_mydata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_bar);
                MydataFragment newFragment = new MydataFragment();

                // FragmentManager를 사용하여 트랜잭션 시작
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // 프래그먼트를 교체하고 트랜잭션을 백스택에 추가 (선택 사항)
                transaction.replace(R.id.tabs_layout, newFragment);
                transaction.addToBackStack(null);
                bottomNavigationView.getMenu().findItem(R.id.tab_mydata).setChecked(true);
                // 트랜잭션 커밋
                transaction.commit();
            }
        });

        ll_loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_bar);
                LoanFragment newFragment = new LoanFragment();

                // FragmentManager를 사용하여 트랜잭션 시작
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // 프래그먼트를 교체하고 트랜잭션을 백스택에 추가 (선택 사항)
                transaction.replace(R.id.tabs_layout, newFragment);
                transaction.addToBackStack(null);
                bottomNavigationView.getMenu().findItem(R.id.tab_loan).setChecked(true);
                // 트랜잭션 커밋
                transaction.commit();
            }
        });



        onDestroy();

        return homeView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/user/profile";
        String finalurl = url + endpoint;

        final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, finalurl,null,
                new Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));
                            // Check for error message
                            if(decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject obj = decryptedResponse.getJSONObject("data");
                            String username=obj.getString("username");
                            String balance=obj.getString("balance");
                            String account_number =obj.getString("account_number");

                            // balance를 숫자로 변환
                            double balanceValue = Double.parseDouble(balance);

                            // 숫자를 1000단위로 쉼표로 나누기 위한 포맷 지정
                            DecimalFormat formatter = new DecimalFormat("#,###");

                            // 포맷을 적용하여 문자열로 변환
                            String formattedBalance = formatter.format(balanceValue);

                            tv_account.setText(account_number);
                            tv_balance.setText(formattedBalance);
                            tv_user.setText(username+" 님 반갑습니다 !!!");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Network error occurred", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers=new HashMap();
                headers.put("Authorization","Bearer "+retrivedToken);
                return headers;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (queue != null) {
            queue.cancelAll(this);
        }
    }

    public void viewQnAList() {
        qRecords.clear();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/qna/viewboard";
        final String finalurl = url + endpoint;
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, finalurl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
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

                        recyclerView_qna.setLayoutManager(new LinearLayoutManager(getActivity()));
                        qadapter = new Qadapter(getActivity(), qRecords);
                        recyclerView_qna.setAdapter(qadapter);


                        Integer count = qadapter.getItemCount();
                        if (count == 0) {
                            recyclerView_qna.setVisibility(View.GONE);
                        } else {
                            recyclerView_qna.setVisibility(View.VISIBLE);
                        }
                        //qadapter.setOnItemClickListener(QnAListView.this);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Something Response wrong", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
                final String retrivedToken = sharedPreferences.getString("accesstoken", null);
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
        queue.getCache().clear();
    }

    public void viewNoticeList() {
        nRecords.clear();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/notice/viewboard";
        final String finalurl = url + endpoint;
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, finalurl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") != 200) {
                                Toast.makeText(getActivity(), "Error: " + decryptedResponse.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show();
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

                        recyclerView_notice.setLayoutManager(new LinearLayoutManager(getActivity()));
                        nadapter = new Nadapter(getActivity(), nRecords);
                        recyclerView_notice.setAdapter(nadapter);

                        Integer count = nadapter.getItemCount();
                        if (count == 0) {
                            recyclerView_notice.setVisibility(View.GONE);
                        } else {
                            recyclerView_notice.setVisibility(View.VISIBLE);
                        }
                        //nadapter.setOnItemClickListener(HomeFragment.this);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Something Response wrong", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
                final String retrivedToken = sharedPreferences.getString("accesstoken", null);
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
        queue.getCache().clear();
    }
}