package com.app.turtlebank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class LoanFragment extends Fragment {

    // LoanFragment 열기
    private void openLoanFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment loanFragment = fragmentManager.findFragmentByTag("LoanFragment");
        fragmentManager.beginTransaction().hide(LoanFragment.this).commit();
        loanFragment = new LoanFragment();
        fragmentManager.beginTransaction().add(((ViewGroup) getView().getParent()).getId(), loanFragment, "LoanFragment").commit();

    }

    // LoanFragment 닫기
    private void closeLoanFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment loanFragment = fragmentManager.findFragmentByTag("LoanFragment");
        if (loanFragment != null && loanFragment.isVisible()) {
            fragmentManager.beginTransaction().hide(loanFragment).commit();
        }
        fragmentManager.beginTransaction().show(LoanFragment.this).commit();
    }

    public interface LoanCallback {
        void onLoanResult(String isLoan) throws JSONException;
    }

    public void Loan(final LoanCallback callback) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/loan/loan";
        String finalurl = url + endpoint;

        final JsonObjectRequest stringRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, finalurl, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject decryptedResponse = new JSONObject(EncryptDecrypt.decrypt(response.get("enc_data").toString()));

                            // Check for error message
                            if (decryptedResponse.getJSONObject("status").getInt("code") == 200) {
                                JSONObject dataObject = decryptedResponse.getJSONObject("data");
                                // Extracting required data
                                JSONArray loanAmountArray = dataObject.getJSONArray("loan_amount");
                                JSONArray accountNumberArray = dataObject.getJSONArray("account_number");
                                JSONArray balanceArray = dataObject.getJSONArray("balance");
                                int statusCode = decryptedResponse.getJSONObject("status").getInt("code");

                                // Constructing JSON object
                                JSONObject loanData = new JSONObject();
                                loanData.put("loan_amount", loanAmountArray);
                                loanData.put("account_number", accountNumberArray);
                                loanData.put("balance", balanceArray);
                                loanData.put("status_code", statusCode);

                                callback.onLoanResult(String.valueOf(loanData));

                            } else if (decryptedResponse.getJSONObject("status").getInt("code") == 400) {
                                JSONObject dataObject = decryptedResponse.getJSONObject("data");
                                // Extracting required data
                                JSONArray accountNumberArray = dataObject.getJSONArray("account_number");
                                int statusCode = decryptedResponse.getJSONObject("status").getInt("code");

                                // Constructing JSON object
                                JSONObject loanData = new JSONObject();
                                loanData.put("account_number", accountNumberArray);
                                loanData.put("status_code", statusCode);

                                callback.onLoanResult(String.valueOf(loanData));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        queue.add(stringRequest);
        queue.getCache().clear();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_loan_roading, container, false);

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Loan(new LoanCallback() {
            @Override
            public void onLoanResult(String loanData) throws JSONException {

                JSONObject jsonObject = new JSONObject(loanData);
                String statusCode = jsonObject.getString("status_code");
//              JSONArray balanceArray = jsonObject.getJSONArray("balance");

                rootView.removeAllViews();

                View loanYView = null;
                View loanNView = null;

                if (statusCode.equals("200")) {
                    // Show fragment_loan_y layout
                    loanYView = inflater.inflate(R.layout.fragment_loan_y, container, false);
                    JSONArray accountNumberArray = jsonObject.getJSONArray("account_number");
                    JSONArray loanAmountArray = jsonObject.getJSONArray("loan_amount");
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    Spinner accountSpinner = loanYView.findViewById(R.id.account_list_spinner_y);
                    ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<String>());
                    accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    for (int i = 0; i < accountNumberArray.length(); i++) {
                        accountAdapter.add(accountNumberArray.getString(i));
                    }
                    accountSpinner.setAdapter(accountAdapter);

                    // Set loan amount to TextView
                    TextView debtBalanceTextView = loanYView.findViewById(R.id.debt_amount_text_view);
                    if (loanAmountArray.length() > 0) {
                        // DecimalFormat 객체를 생성합니다.
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        // 포맷을 적용하여 숫자를 문자열로 변환합니다.
                        String formattedNumber = formatter.format(loanAmountArray.getInt(0));
                        debtBalanceTextView.setText("대출 잔액: " + formattedNumber);
                    }

                    // 상환버튼
                    final Button repaymentButton = loanYView.findViewById(R.id.repayment_debt_button);
                    repaymentButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onClick(View v) {
                            repayment_debt(rootView);
                            Toast.makeText(getActivity().getApplicationContext(), "상환되었습니다.", Toast.LENGTH_SHORT).show();

                            openLoanFragment();
                        }
                    });

                    // 취소버튼
                    final Button cancelDebtButton = loanYView.findViewById(R.id.cancel_debt_button);
                    cancelDebtButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onClick(View v) {
                            cancel_debt(rootView);
                            Toast.makeText(getActivity().getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            openLoanFragment();
                        }
                    });

                    rootView.addView(loanYView, params);

                } else if (statusCode.equals("400")) {
                    // Show fragment_loan_n layout
                    loanNView = inflater.inflate(R.layout.fragment_loan_n, container, false);
                    JSONArray accountNumberArray = jsonObject.getJSONArray("account_number");
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    Spinner accountSpinner = loanNView.findViewById(R.id.account_list_spinner_n);
                    ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<String>());
                    accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    for (int i = 0; i < accountNumberArray.length(); i++) {
                        accountAdapter.add(accountNumberArray.getString(i));
                    }
                    accountSpinner.setAdapter(accountAdapter);

                    // 대출버튼
                    final Button getDebtButton = loanNView.findViewById(R.id.get_debt_button);
                    getDebtButton.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onClick(View v) {
                            get_debt(rootView);
                            Toast.makeText(getActivity().getApplicationContext(), "대출이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            openLoanFragment();
                        }
                    });

                    rootView.addView(loanNView, params);
                }
            }
        });

        return rootView;
    }

    // Enter the correct url for your api service site
    final int initialTimeoutMs = 2000; // 초기 타임아웃 값 (5초)
    final int maxNumRetries = 0; // 최대 재시도 횟수
    final float backoffMultiplier = 1f; // 재시도 간격의 배수

    RetryPolicy policy = new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier);

    // 대출 버튼 함수
    public void get_debt(View rootView) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/loan/get_debt";
        String finalurl = url + endpoint;

        // 파라미터 설정
        JSONObject requestBody = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();
        try {
            // fragment_loan_n에서 선택한 account_number 가져오기
            Spinner accountSpinner = rootView.findViewById(R.id.account_list_spinner_n);
            String accountNumber = accountSpinner.getSelectedItem().toString();

            // 서울시간 현재 시간 구하기
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String seoulTime = sdf.format(new Date());

            // 요청에 필요한 데이터 설정
            requestBody.put("account_number", accountNumber);
            requestBody.put("loan_amount", "50000000");
            requestBody.put("loan_time", seoulTime);

            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestBody.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 요청 보내기
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 대출 요청 성공 시 처리
                        try {
                            String message = response.getJSONObject("data").getString("message");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 대출 요청 실패 시 처리
                        Toast.makeText(getActivity(), "대출 요청 실패", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
            @Override
            public RetryPolicy getRetryPolicy() {
                // RetryPolicy 설정
                return policy;
            }
        };

        queue.add(request);
    }

    // 상환 버튼 함수
    public void repayment_debt(View rootView) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/loan/repayment";
        String finalurl = url + endpoint;

        // 파라미터 설정
        JSONObject requestBody = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            // fragment_loan_y에서 선택한 대출금액 입력 EditText 찾기
            EditText repaymentAmountEditText = rootView.findViewById(R.id.repayment_amount);
            String repaymentAmount = repaymentAmountEditText.getText().toString();

            // fragment_loan_y에서 선택한 account_number 가져오기
            Spinner accountSpinner = rootView.findViewById(R.id.account_list_spinner_y);
            String accountNumber = accountSpinner.getSelectedItem().toString();

            // 요청에 필요한 데이터 설정
            requestBody.put("selected_account", accountNumber);
            requestBody.put("repayment_amount", repaymentAmount);

            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestBody.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 요청 보내기
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 상환 요청 성공 시 처리
                        try {
                            String message = response.getJSONObject("data").getString("message");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 대출 요청 실패 시 처리
                        Toast.makeText(getActivity(), "대출 요청 실패", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        queue.add(request);
    }

    // 취소 버튼 함수
    public void cancel_debt(View rootView) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jwt", Context.MODE_PRIVATE);
        final String retrivedToken = sharedPreferences.getString("accesstoken", null);
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("apiurl", Context.MODE_PRIVATE);
        final String url = sharedPreferences.getString("apiurl", null);
        String endpoint = "/api/loan/loan_cancel";
        String finalurl = url + endpoint;

        // 파라미터 설정
        JSONObject requestBody = new JSONObject();
        JSONObject requestDataEncrypted = new JSONObject();

        try {
            // fragment_loan_n에서 선택한 account_number 가져오기
            Spinner accountSpinner = rootView.findViewById(R.id.account_list_spinner_y);
            String accountNumber = accountSpinner.getSelectedItem().toString();

            // 요청에 필요한 데이터 설정
            requestBody.put("selected_account", accountNumber);

            requestDataEncrypted.put("enc_data", EncryptDecrypt.encrypt(requestBody.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 요청 보내기
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, finalurl, requestDataEncrypted,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 대출 요청 성공 시 처리
                        try {
                            String message = response.getJSONObject("data").getString("message");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 대출 요청 실패 시 처리
                        Toast.makeText(getActivity(), "대출 요청 실패", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + retrivedToken);
                return headers;
            }
        };

        queue.add(request);
    }
}