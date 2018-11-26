package com.outsmart.outsmartpicker;

import android.Manifest;
import android.annotation.SuppressLint;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.util.Log;

import com.outsmart.outsmartpicker.utils.FileUtils;
import com.outsmart.outsmartpicker.utils.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;

/**
 * Created by carlos on 08/01/18.
 */
@RuntimePermissions
public class MediaPicker extends Fragment {
    public final static String PICKER_RESPONSE_FILTER = "com.outsmart.picker.RESPONSE";
    public final static String PICKER_INTENT_FILE = "file";
    public final static String PICKER_INTENT_FILE_THUMB = "fileThumbnail";

    private static final String TAG = MediaPicker.class.getSimpleName();
    public static final String PICKER_INTENT_ERROR = "com.outsmart.picker.ERROR";
    public final int PICK_CAMERA_REQUEST = 5179;

    private HandlerThread workingThread;
    private Handler workingHandler;

    private String fileImagePath;
    private String fileVideoPath;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        workingThread = new HandlerThread("workingThread");
        workingThread.start();
        workingHandler = new Handler(workingThread.getLooper());

        if (bundle != null) {
            fileImagePath = bundle.getString("fileImagePath");
            fileVideoPath = bundle.getString("fileVideoPath");
        }
    }

    @Override
    public void onDestroy() {
        workingHandler = null;
        workingThread.quit();
        workingThread = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("fileImagePath", fileImagePath);
        bundle.putString("fileVideoPath", fileVideoPath);
    }


    public void pickMediaWithPermissions(MediaType type) {
        MediaPickerPermissionsDispatcher.pickMediaWithPermissionCheck(this, type);
    }

    @NeedsPermission(value = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    })
    public void pickMedia(MediaType type) {
        File directory = getActivity().getExternalCacheDir();
        try {
            String fileName = UUID.randomUUID().toString();
            File fileImage = File.createTempFile(fileName, ".jpg", directory);
            File fileVideo = File.createTempFile(fileName, ".mp4", directory);
            this.fileImagePath = fileImage.getAbsolutePath();
            this.fileVideoPath = fileVideo.getAbsolutePath();

            String authority = getActivity().getPackageName();

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), authority, fileImage));

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), authority, fileVideo));

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            Intent[] intentArray = null;

            switch (type) {
                case IMAGE:
                    contentSelectionIntent.setType("image/*");
                    intentArray = new Intent[]{takePictureIntent};
                    break;
                case VIDEO:
                    contentSelectionIntent.setType("video/*");
                    intentArray = new Intent[]{takeVideoIntent};
                    break;
                case IMAGE_OR_VIDEO:
                    contentSelectionIntent.setType("video/*");
                    contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                    intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                    break;
            }
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, PICK_CAMERA_REQUEST);
        } catch (IOException ex) {
            Log.d(TAG, "create temporal file not created");
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intentResponse = new Intent(PICKER_RESPONSE_FILTER);
        File outFile = null;
        File fileImage = new File(fileImagePath);
        File fileVideo = new File(fileVideoPath);
        Boolean success = false;

        String authority = getActivity().getPackageName();
        if (resultCode == RESULT_OK && requestCode == PICK_CAMERA_REQUEST) {
            if (data != null && data.getData() != null && !data.getData().getAuthority().equalsIgnoreCase(authority)) { // from gallery
                fileImage.delete();
                fileVideo.delete();

                String fileStr = FileUtils.getPath(getActivity(), data.getData());
                if(fileStr == null) {
                    intentResponse.putExtra(PICKER_INTENT_ERROR, "file is not in local storage");
                    getActivity().sendBroadcast(intentResponse);
                    return;
                }
                outFile = new File(fileStr);
                success = true;
            } else { //from camera
                if (fileVideo.length() == 0) {
                    outFile = fileImage.length() > 0 ? fileImage : null;
                    fileVideo.delete();
                } else if (fileImage.length() == 0) {
                    outFile = fileVideo.length() > 0 ? fileVideo : null;
                    fileImage.delete();
                }

                if (outFile != null) success = true;
            }
        } else {
            fileImage.delete();
            fileVideo.delete();
        }
        if (success) {
            final File responseFile = outFile;
            workingHandler.post(new Runnable() {
                @Override
                public void run() {
                    String mime = FileUtils.getMimeType(responseFile);
                    Bitmap thumbnail = MediaUtils.getThumbnail(getActivity(), responseFile, mime);
                    intentResponse.putExtra(PICKER_INTENT_FILE, responseFile.getAbsolutePath());
                    intentResponse.putExtra(PICKER_INTENT_FILE_THUMB, thumbnail);
                    getActivity().sendBroadcast(intentResponse);
                }
            });
        }
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MediaPickerPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}

