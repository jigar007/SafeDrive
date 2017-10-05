package com.unimelb.jigarthakkar.safedrivesystem;

/**
 * Created by Tang on 10/2/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView pname;
    TextView pemail;

    Button bt_signout;

    private SharedPreferences shared;
    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        shared = getSharedPreferences("userdata", Activity.MODE_PRIVATE);
        editor = shared.edit();

        // Read data
        String name = shared.getString("name", "");
        String email = shared.getString("email", "");

        // Show data
        pname = (TextView) findViewById(R.id.profile_name);
        pname.setText(name);

        pemail = (TextView) findViewById(R.id.profile_email);
        pemail.setText(email);

        // Sign out
        bt_signout = (Button) findViewById(R.id.sign_out);

        // On sign out,
        // clear all activity data,
        // and prevent "back" button from opening logged-in-only activities.
        bt_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear data
                editor.clear();
                editor.putBoolean("isLogin", false);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), LoginSignupActivity.class);
                intent.putExtra("finish", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

}
