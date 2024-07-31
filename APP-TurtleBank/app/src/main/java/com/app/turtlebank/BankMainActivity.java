package com.app.turtlebank;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class BankMainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private AccountListFragment accountListFragment = new AccountListFragment();
    private TransactionFragment transactionFragment = new TransactionFragment();
    private HomeFragment homeFragment = new HomeFragment();
    private LoanFragment loanFragment = new LoanFragment();
    private MydataFragment mydataFragment = new MydataFragment();



    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        System.exit(0);
                    }
                }).create().show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.tabs_layout, homeFragment).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_bar);
        bottomNavigationView.getMenu().findItem(R.id.tab_home).setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        Log.d("drdf","dfdf" );

//        if(RootUtil.isDeviceRooted()) {
//            Toast.makeText(getApplicationContext(), "Phone is Rooted", Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }

    // 메뉴 리소스 XML의 내용을 앱바(App Bar)에 반영
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate (R.menu.toolbar_menu, menu);

        return true;
    }

    //앱바(App Bar)에 표시된 액션 또는 오버플로우 메뉴가 선택되면
    //액티비티의 onOptionsItemSelected() 메서드가 호출
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.toolbar_logout:
                SharedPreferences pref = getApplicationContext().getSharedPreferences("jwt", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isloggedin", false);
                editor.apply();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return true;
            case R.id.toolbar_mypage:
                startActivity(new Intent(getApplicationContext(), Mypage.class));
                return true;
            default:
                return super.onOptionsItemSelected (item);
        }
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (menuItem.getItemId()) {
                case R.id.tab_account_list:
                    transaction.replace(R.id.tabs_layout, accountListFragment).commitAllowingStateLoss();
                    break;
                case R.id.tab_transaction:
                    transaction.replace(R.id.tabs_layout, transactionFragment).commitAllowingStateLoss();
                    break;
                case R.id.tab_home:
                    transaction.replace(R.id.tabs_layout, homeFragment).commitAllowingStateLoss();
                    break;
                case R.id.tab_loan:
                    transaction.replace(R.id.tabs_layout, loanFragment).commitAllowingStateLoss();
                    break;
                case R.id.tab_mydata:
                    transaction.replace(R.id.tabs_layout, mydataFragment).commitAllowingStateLoss();
                    break;
            }

            return true;
        }
    }


}