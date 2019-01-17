# MediaPicker

Picks images and videos in android devices.

## How to setup
First add the JitPack repository to your root build.gradle at the end of repositories

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        }
    }
```

Second add the dependency.
```gradle
dependencies {
    implementation 'com.github.cahemunoz:MediaPicker:1.1.5'
}
```

Next step, create a 'res/xml/provider_path.xml' file with this content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="cache" path="Android/data/<your applicationId value>/cache" />
</paths>
```
Ensure to replace the path value with  your applicationId value (you can find it on build.gradle file).



And last step, add the file provider configuration to your AndroidManifest.xml file
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/provider_path" />
```


## How to use it
Add the MediaPicker fragment to your Activity or Fragment and wait a response Intent in a BroadcastReceiver, just like this:
```java
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
```


The response Intent has a two objects, the first is the path of the picked file, and the second is a Bitmap thumbnail of the image or video file.

Also if the was an error, the response Intent has a variable with the the details, check it first.



## F.A.Q
##### When I choose a file from google drive sometimes doesn't work ?
This happens because the file is not in local storage, on the next version I try to fix that, or you can open a PR.


