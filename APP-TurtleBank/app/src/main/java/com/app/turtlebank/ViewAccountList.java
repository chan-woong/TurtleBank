package com.app.turtlebank;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewAccountList extends AppCompatActivity implements Badapter.OnItemClickListener {

    private TextView emptyView;

    public static final String account_number="account_number";
    public static final String bank_code="bank_code";
    public static final String balance="balance";
    public static final String username="username";

    RecyclerView recyclerView;
    List<BankAccount> accounts;
    Badapter badapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewbenif);
//        recyclerView=findViewById();
    }



    @Override
    public void onItemClick(int position) {}
}


