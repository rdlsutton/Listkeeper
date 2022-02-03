package com.radlsuttonedmn.listkeeper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adjust the sampling size and rotation of photographs
 */

class PhotoAdjuster {

    // Compute the optimum sampling size based on the image view dimensions
    private static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {

        // Raw height and width of the image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > requiredHeight || width > requiredWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width
            // larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= requiredHeight && (halfWidth / inSampleSize) >= requiredWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Fetch the bitmap using the optimum sampling size and rotate it as needed
    static Bitmap getAdjustedBitmap(Activity activity, String imageUriString, int width, int height) {

        Bitmap bitmap;

        // If the photo intent was canceled, the imagePath will be blank
        if (imageUriString == null || imageUriString.equals("")) {
            return null;
        }

        // Convert saved image URI string to a URI
        Uri imageUri = Uri.parse(imageUriString);

        // First decode with inJustDecodeBounds = true to check the dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {

            // Might have imported an invalid image path from an imported file
            return null;
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode the bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {

            // Might have imported an invalid image path from an imported file
            return null;
        }

        // Pause the thread in case it is taking a long time to decode the image file
        if (bitmap == null) {
            try {
                Thread.sleep(400);
                try {
                    InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {

                    // Might have imported an invalid image path from an imported file
                    return null;
                }
            } catch (InterruptedException e) {
                return null;
            }
        }

        // If the bitmap is still not available, return null
        if (bitmap == null) {
            return null;
        }

        // Use ExifInterface to get the orientation and rotate the image accordingly
        Matrix matrix = new Matrix();
            try {
            InputStream inStream = activity.getContentResolver().openInputStream(imageUri);
            if (inStream != null) {
                ExifInterface exifInterface = new ExifInterface(inStream);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
                inStream.close();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
