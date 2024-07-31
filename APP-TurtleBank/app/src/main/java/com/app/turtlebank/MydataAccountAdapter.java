package com.app.turtlebank;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MydataAccountAdapter extends RecyclerView.Adapter<MydataAccountAdapter.ViewHolder> {


    ArrayList<BankAccount> MyBankAccounts;
    Activity context;


    public MydataAccountAdapter(ArrayList<BankAccount> myData, Activity activity) {
        this.MyBankAccounts = myData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mydata_account_cardview,parent,false);
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

            holder.secondLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 클릭 이벤트를 처리하는 메소드 호출
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        ((Mydata_send) context).click(holder, adapterPosition);
                    }
                    Log.d("API_RESPONSE", "12312321312312312: " + holder.textviewbankno.getText().toString());
                    Intent intent = new Intent(context, Mydata_sendMoney.class);
                    intent.putExtra("account_number", holder.textviewbankno.getText().toString());
                    ((Mydata_send) context).startActivityForResult(intent, 1);
                    //context.startActivity(intent);
                }
            });
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
        LinearLayout secondLinearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textviewbankno = itemView.findViewById(R.id.text_view_bank_account_no);
            textviewmoney = itemView.findViewById(R.id.text_view_bank_account_money);
            textviewcode = itemView.findViewById(R.id.text_view_bank_code);
            secondLinearLayout = itemView.findViewById(R.id.secondLinearLayout);
        }
    }
}
