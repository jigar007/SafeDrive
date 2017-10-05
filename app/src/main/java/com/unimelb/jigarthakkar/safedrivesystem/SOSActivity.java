package com.unimelb.jigarthakkar.safedrivesystem;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SOSActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Button police;
    private Button family;
    private Button insurance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos2);

        police = (Button)findViewById(R.id.callpolice);
        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
              //  Toast.makeText(SOSActivity.this,readContacts(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "110"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        family = (Button)findViewById(R.id.callfamily);
        family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
              //  Toast.makeText(SOSActivity.this,readContacts(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "13878471868"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        insurance = (Button)findViewById(R.id.callinsurance);
        insurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
               // Toast.makeText(SOSActivity.this,readContacts(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "666666"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(SOSActivity.this, MainActivity.class);
        startActivity(intent);

    }
    private String readContacts() {
        Cursor cursor = null;
        String name = "";
        String number = "";
        try {
            ContentResolver resolver = getContentResolver();
            cursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        number = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.v("woider", "Name:" + name + "\tPhone:" + number);


            }
            System.out.print(name + number);


        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();

            }
        }
        return name + number;
    }
    //EditText text = (EditText)findViewById(R.id.callpolice);
    //String match = text.getText().toString();

}
