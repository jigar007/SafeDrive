package com.unimelb.jigarthakkar.safedrivesystem;
/**  * Created by jiameng on 10/4/17.  */


import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class SOSActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ImageButton police;
    // set a Button to SMS police

    private ImageButton family;
    //set a Button to SMS family

    private ImageButton insurance;
    // set a Button to SMS insurance

    private ImageButton policesms;
    private ImageButton familysms;
    private ImageButton insurancesms;
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* interact with background data to get each users emergency contact 
           * and insurance number */


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos2);
        police = (ImageButton)findViewById(R.id.callpolice);
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
        family = (ImageButton)findViewById(R.id.callfamily);
        family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //  Toast.makeText(SOSActivity.this,readContacts(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                SharedPreferences sharedPreferences = getSharedPreferences("userdata",Activity.MODE_PRIVATE);
                String familyNumber = sharedPreferences.getString("family","not found");
                intent.setData(Uri.parse("tel:" + familyNumber));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        insurance = (ImageButton)findViewById(R.id.callinsurance);
        insurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // Toast.makeText(SOSActivity.this,readContacts(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                SharedPreferences sharedPreferences = getSharedPreferences("userdata",Activity.MODE_PRIVATE);
                String insuranceNumber = sharedPreferences.getString("insurance","not found");
                intent.setData(Uri.parse("tel:" + insuranceNumber));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        policesms = (ImageButton)findViewById(R.id.police);
        policesms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentLocation","not found");
                String message = test + "  help me ! Traffic Accident!";
                SendSMS("110",message);

            }
        });
        familysms = (ImageButton)findViewById(R.id.family);
        familysms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentLocation","not found");
                String message = test + "  help me ! Traffic Accident!";

                SharedPreferences sharedPreferences1 = getSharedPreferences("userdata",Activity.MODE_PRIVATE);
                String familycontact =  sharedPreferences1.getString("family","not found");
                SendSMS(familycontact,message);


            }
        });
        insurancesms = (ImageButton)findViewById(R.id.insurance);
        insurancesms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentLocation","not found");
                String message = test + "  help me ! Traffic Accident!";
                SharedPreferences sharedPreferences1 = getSharedPreferences("userdata",Activity.MODE_PRIVATE);
                String insurancecontact =  sharedPreferences1.getString("insurance","not found");
                SendSMS(insurancecontact,message);

            }
        });

    }
    @Override
    public void onBackPressed(){
        /*It is a function to find the phone's contacts and take a call.*/

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
    public void SendSMS(String phoneNumber,String message){
        /* it is a function to allow this app to send a message */
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sendIntent= PendingIntent.getBroadcast(SOSActivity.this, 0, sentIntent,
                0);
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
        // create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent backIntent= PendingIntent.getBroadcast(SOSActivity.this, 0,
                deliverIntent, 0);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
        intent.putExtra("sms_body", message);
        startActivity(intent);
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sendIntent, backIntent);
        }
    }

}