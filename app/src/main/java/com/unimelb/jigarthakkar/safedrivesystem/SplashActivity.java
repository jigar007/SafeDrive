package com.unimelb.jigarthakkar.safedrivesystem;

/**
 * Created by Tang on 9/28/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    private Intent intent;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("userdata", Activity.MODE_PRIVATE);

        Boolean isLogin = preferences.getBoolean("isLogin", false);




        if(!isLogin) {
            startLoginSignupActivity();
        } else {
            startMainActivity();
        }
    }

    private void startLoginSignupActivity() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                intent = new Intent(SplashActivity.this, LoginSignupActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
            // execution time 2s
        }, 1500); // 1.5s
    }

    private void startMainActivity() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
            intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
            }
        }, 1500);
    }

}
