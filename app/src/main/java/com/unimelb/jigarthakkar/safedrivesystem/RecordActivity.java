package com.unimelb.jigarthakkar.safedrivesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
        List<String> data = getData();
        adapter = new ArrayAdapter<String>(RecordActivity.this, R.layout.record_item, data);
        listView.setAdapter(adapter);


    }

    public static List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("test1");
        data.add("test2");
        data.add("test3");
        return data;

    }

    private void initialListView() {
        listView = (ListView)findViewById(R.id.recordList);
    }

    private void initialLayout() {
        layout = (LinearLayout) findViewById(R.id.recordLayout);
    }

    private void initialRecordView() {
        recordItem = (TextView)findViewById(R.id.textview);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RecordActivity.this, MainActivity.class);
        startActivity(intent);
        RecordActivity.this.finish();
    }
}
