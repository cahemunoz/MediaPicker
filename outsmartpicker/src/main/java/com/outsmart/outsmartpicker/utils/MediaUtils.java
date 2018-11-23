package com.outsmart.outsmartpicker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.File;

/**
 * Created by carlos on 09/01/18.
 */

public class MediaUtils {
    private static final String TAG = MediaUtils.class.getSimpleName();

    public static Bitmap getThumbnail(Context context, File file, String mime) {
        if (mime.contains("video"))
            return extractThumbnail(file);
        else if (mime.contains("image"))
            return decodeUriAsBitmap(context, Uri.fromFile(file), 80, 80);
        else
            return null;
    }


    private static Bitmap extractThumbnail(File file) {
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        return bMap;
    }

    private static Bitmap decodeUriAsBitmap(Context context, Uri uri, int targetWidth, int targetHeight) {
        Bitmap bitmap = null;
        int rotationInDegrees = 0;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            if (targetHeight == 0) {
                targetHeight = (int) (options.outHeight * targetWidth / (double) options.outWidth);
            }

            if (targetWidth == 0) {
                targetWidth = (int) (options.outWidth * targetHeight / (double) options.outHeight);
            }

            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            ExifInterface exif = getExifFromImageUri(uri, context);

            rotationInDegrees = exifToDegrees(exif);
            if (rotationInDegrees > 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(rotationInDegrees);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.d(TAG, "Decoding from Uri");
        }
        return bitmap;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public static int exifToDegrees(ExifInterface exifInterface) {
        int rotation = 0;
        if (exifInterface == null) return rotation;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotation;
    }

    private static ExifInterface getExifFromImageUri(Uri uri, Context context) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(context.getContentResolver().openInputStream(uri));
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage(), ex);
        }
        return exifInterface;
    }
}
