package com.outsmart.picker;

import androidx.fragment.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.outsmart.outsmartpicker.MediaPicker;
import com.outsmart.outsmartpicker.MediaType;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button button;
    TextView textView;

    // Add the MediaPicker Fragment, this is a headless fragment
    MediaPicker mediaPicker;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        // Add the MediaPicker in a FragmentTransaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mediaPicker = new MediaPicker();
        transaction.add(mediaPicker, "mediaPicker");
        transaction.commit();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("");

                // Request a image or video, if you don't have permissions the picker requests them
                mediaPicker.pickMediaWithPermissions(MediaType.IMAGE_OR_VIDEO);

            }
        });


        // Add a broadcast receiver with MediaPicker.PICKER_RESPONSE_FILTER Intent Filter
        registerReceiver(pickerChoose, new IntentFilter(MediaPicker.PICKER_RESPONSE_FILTER));
    }

    //Just process the response intent
    BroadcastReceiver pickerChoose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MediaPicker.PICKER_INTENT_ERROR) != null) {
                Log.d(TAG, intent.getStringExtra(MediaPicker.PICKER_INTENT_ERROR));
            } else {
                File file = new File(intent.getStringExtra(MediaPicker.PICKER_INTENT_FILE));
                Bitmap thumb = intent.getParcelableExtra(MediaPicker.PICKER_INTENT_FILE_THUMB);
                textView.setText(file.getAbsolutePath());
                imageView.setImageBitmap(thumb);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget unregister the broadcast
        unregisterReceiver(pickerChoose);
    }

}
