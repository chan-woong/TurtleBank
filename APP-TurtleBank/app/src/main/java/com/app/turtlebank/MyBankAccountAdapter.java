package com.app.turtlebank;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyBankAccountAdapter extends RecyclerView.Adapter<MyBankAccountAdapter.ViewHolder> {


    ArrayList<BankAccount> MyBankAccounts;
    Activity context;

    public MyBankAccountAdapter(ArrayList<BankAccount> myData, Activity activity) {
        this.MyBankAccounts = myData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bank_account_cardview,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



        if (position < MyBankAccounts.size()) {
            final BankAccount account = MyBankAccounts.get(position);
            // DecimalFormat 객체를 생성합니다.
            DecimalFormat formatter = new DecimalFormat("#,###");
            // 포맷을 적용하여 숫자를 문자열로 변환합니다.
            String formattedNumber = formatter.format(account.getBalance());

            holder.textviewmoney.setText(formattedNumber);
            holder.textviewbankno.setText(String.valueOf(account.getAccount_number()));
            holder.textviewcode.setText(String.valueOf(account.getBank_code()));
        }
    }

    @Override
    public int getItemCount() {
        return MyBankAccounts.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textviewbankno;
        TextView textviewmoney;
        TextView textviewcode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textviewbankno = itemView.findViewById(R.id.text_view_bank_account_no);
            textviewmoney = itemView.findViewById(R.id.text_view_bank_account_money);
            textviewcode = itemView.findViewById(R.id.text_view_bank_code);
        }
    }
}
