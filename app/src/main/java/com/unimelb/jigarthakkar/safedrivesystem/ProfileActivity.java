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
import android.widget.ImageButton;
import android.widget.TextView;
import java.lang.String;

public class ProfileActivity extends AppCompatActivity {

    TextView pname;
    TextView pemail;
    TextView joinDate;
    TextView protectDay;

    ImageButton bt_signout;
    ImageButton bt_edit_contact;

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

        String date = shared.getString("date", "");
        String splitString[] = date.split(" ");
        String loginDate = splitString[0];

        String today = TimeUtil.getTime();
        String days = TimeUtil.getTimeDifference(loginDate, today);

        // Show data
        pname = (TextView) findViewById(R.id.profile_name);
        pname.setText(name);

        pemail = (TextView) findViewById(R.id.profile_email);
        pemail.setText(email);

        joinDate = (TextView) findViewById(R.id.date_number);
        joinDate.setText(date);

        protectDay = (TextView) findViewById(R.id.days_number);
        protectDay.setText(days + " days");

        // Edit emergency contact
        bt_edit_contact = (ImageButton) findViewById(R.id.edit_contact);

        bt_edit_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), EditContactActivity.class);
                startActivity(intent);
            }
        });

        // Sign out
        bt_signout = (ImageButton) findViewById(R.id.bt_logout);

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
