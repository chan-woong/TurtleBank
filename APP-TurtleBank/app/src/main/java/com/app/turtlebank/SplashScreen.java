package com.app.turtlebank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIMER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.splash_screen);

        ImageView toplogo = findViewById(R.id.toplogo);
        ImageView bottomlogo = findViewById(R.id.bottomlogo);

        Animation flyFromLeft = AnimationUtils.loadAnimation(this, R.anim.fly_from_left);
        Animation flyFromRight = AnimationUtils.loadAnimation(this, R.anim.fly_from_right);

        toplogo.startAnimation(flyFromRight);
        bottomlogo.startAnimation(flyFromLeft);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMER);
    }
}
