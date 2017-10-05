package com.unimelb.jigarthakkar.safedrivesystem;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;



import java.util.List;

public class SOSActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Button police;
    private Button family;
    private Button insurance;
    private Button policesms;
    private Button familysms;
    private Button insurancesms;


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
        policesms = (Button)findViewById(R.id.police);
        policesms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentAddress","not found");
                String message = test + "  help me ! Traffic Accident!";
                SendSMS("110",message);

            }
        });
        familysms = (Button)findViewById(R.id.family);
        familysms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentAddress","not found");
                String message = test + "  help me ! Traffic Accident!";
                String familycontact =  sharedPreferences.getString("familycontact","not found");
                SendSMS(familycontact,message);


            }
        });
        insurancesms = (Button)findViewById(R.id.insurance);
        insurancesms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences sharedPreferences = getSharedPreferences("test",Activity.MODE_PRIVATE);
                String test= sharedPreferences.getString("currentAddress","not found");
                String message = test + "  help me ! Traffic Accident!";
                String insurancecontact =  sharedPreferences.getString("insurancecontact","not found");
                SendSMS(insurancecontact,message);

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
public void SendSMS(String phoneNumber,String message){
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