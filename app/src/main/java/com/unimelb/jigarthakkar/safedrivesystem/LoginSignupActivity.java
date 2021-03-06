package com.unimelb.jigarthakkar.safedrivesystem;

/**
 * Created by Tang on 9/28/17.
 */

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class LoginSignupActivity extends AppCompatActivity {

    Button login;
    Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        login= (Button) findViewById(R.id.log);
        signup = (Button) findViewById(R.id.sign);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signup = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(signup);
            }
        });
        // set the background animation
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.main);
        //AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        //animationDrawable.setEnterFadeDuration(1250);
        //animationDrawable.setExitFadeDuration(2500);
        //animationDrawable.start();
    }

}
