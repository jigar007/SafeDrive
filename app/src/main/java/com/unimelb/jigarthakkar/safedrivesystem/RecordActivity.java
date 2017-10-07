package com.unimelb.jigarthakkar.safedrivesystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class RecordActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private LinearLayout layout;
    private TextView recordItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initialLayout();
        initialListView();
        initialRecordView();
        SharedPreferences prefs = getSharedPreferences("test", Activity.MODE_PRIVATE);
        Log.d("tag",prefs.getString("visitedLocation", "defaultStringIfNothingFound"));

        String[] separated = prefs.getString("visitedLocation", "defaultStringIfNothingFound").split("\n");

        List<String> data = Arrays.asList(separated);
//        adapter = new ArrayAdapter<String>(RecordActivity.this, R.layout.record_item, data);
//        listView.setAdapter(adapter);
        listView.setAdapter( new com.gnetspace.customlistview.CustomAdapter1(this, separated));
    }

    private void initialListView() {
        listView = (ListView)findViewById(R.id.recordList);
    }

    private void initialLayout() {
        layout = (LinearLayout) findViewById(R.id.recordLayout);
    }

    private void initialRecordView() {
//        recordItem = (TextView)findViewById(R.id.textview);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RecordActivity.this, MainActivity.class);
        startActivity(intent);
        RecordActivity.this.finish();
    }
}