package com.app.turtlebank;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<JSONObject> transactionList;
    private Context context;

    public TransactionAdapter(Context context, List<JSONObject> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bank_transaction_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject transactionData = transactionList.get(position);

            //송금시간
            String sendtime = transactionData.getString("sendtime");

            sendtime = sendtime.replace("T", " ");
            sendtime = sendtime.replaceAll("\\.\\d{3}Z", "");

            //송금뱅크
            String from_bankcode = transactionData.getString("from_bankcode");
            if(from_bankcode.equals("333")){
                from_bankcode = "송금뱅크 : RabbitBank";
            }else if(from_bankcode.equals("555")){
                from_bankcode = "송금뱅크 : TurtleBank";
            }

            //송금계좌
            String from_account = transactionData.getString("from_account");
            from_account = "송금계좌 :" + from_account;

            //수취뱅크
            String to_bankcode = transactionData.getString("to_bankcode");
            if(to_bankcode.equals("333")){
                to_bankcode = "수취뱅크 : RabbitBank";
            }else if(to_bankcode.equals("555")){
                to_bankcode = "수취뱅크 : TurtleBank";
            }

            //수취계좌
            String to_account = transactionData.getString("to_account");
            to_account = "수취계좌 :" + to_account;

            //금액
            String amount = transactionData.getString("amount");
            int numericAmount = Integer.parseInt(amount);
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            amount = numberFormat.format(numericAmount) + "원";


            // TextView 설정
            holder.sendTime.setText(sendtime);
            holder.fromBank.setText(from_bankcode);
            holder.fromAccount.setText(from_account);
            holder.toBank.setText(to_bankcode);
            holder.toAccount.setText(to_account);
            holder.amount.setText(amount);

            Log.d("TransactionAdapter", "Item at position " + position + " bound successfully.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sendTime;
        TextView fromBank;
        TextView fromAccount;
        TextView toBank;
        TextView toAccount;
        TextView amount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // TextView 초기화
            sendTime = itemView.findViewById(R.id.text_view_transaction_time);
            fromBank = itemView.findViewById(R.id.text_view_transaction_sender_bank);
            fromAccount = itemView.findViewById(R.id.text_view_transaction_sender_account);
            toBank = itemView.findViewById(R.id.text_view_transaction_receiver_bank);
            toAccount = itemView.findViewById(R.id.text_view_transaction_receiver_account);
            amount = itemView.findViewById(R.id.text_view_transaction_balance);
        }
    }
}