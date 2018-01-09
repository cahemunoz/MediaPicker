package com.outsmart.picker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.outsmart.outsmartpicker.MediaPicker;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button button;
    TextView textView;
    MediaPicker mediaPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        //*/

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mediaPicker = new MediaPicker();
        transaction.add(mediaPicker, "mediaPicker");
        transaction.commit();
        //*/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("");
                mediaPicker.pickMediaWithPermissions();
            }
        });
        registerReceiver(pickerChoose, new IntentFilter(MediaPicker.PICKER_RESPONSE_FILTER));
    }

    BroadcastReceiver pickerChoose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            File file = new File(intent.getStringExtra(MediaPicker.PICKER_INTENT_FILE));
            textView.setText(file.getAbsolutePath());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pickerChoose);
    }

}
