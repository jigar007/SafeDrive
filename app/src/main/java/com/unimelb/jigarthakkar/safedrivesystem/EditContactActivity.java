package com.unimelb.jigarthakkar.safedrivesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditContactActivity extends AppCompatActivity {

    Button cancel;
    Button save;

    EditText family_contact;
    EditText insurance_contact;

    String family;
    String insurance;

    private SharedPreferences shared;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        // Cancel and Save buttons
        cancel = (Button) findViewById(R.id.contact_cancel);
        save = (Button) findViewById(R.id.contact_save);

        shared = getSharedPreferences("userdata", Activity.MODE_PRIVATE);
        editor = shared.edit();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("finish", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                family_contact = (EditText) findViewById(R.id.family_contact);
                insurance_contact = (EditText) findViewById(R.id.insurance_contact);

                family = family_contact.getText().toString();
                insurance = insurance_contact.getText().toString();

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        try {
                            if(family.length() == 0 || insurance.length() == 0) {

                                runOnUiThread(new Runnable() {

                                    public void run() {

                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Please provide both!", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                });

                            } else {

                                runOnUiThread(new Runnable() {

                                    public void run() {

                                        Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                        startActivity(intent);
                                    }
                                });

                                // Save data
                                editor.putString("family", family);
                                editor.putString("insurance", insurance);
                                editor.commit();
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            Log.d("Error", "catching the error");
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

}
