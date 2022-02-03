package com.radlsuttonedmn.listkeeper;

import android.graphics.Bitmap;

import java.util.HashMap;

class BitmapStore {

    private final HashMap<String, Bitmap> mBitmaps;

    // Constructor that initializes bitmap hash map
    BitmapStore() {
        mBitmaps = new HashMap<>();
    }

    // Add a new bitmap to the bitmap array
    void addBitmap(String imageUriString, Bitmap bitmap) {
        mBitmaps.put(imageUriString, bitmap);
    }

    // Fetch the bitmap associated with the specified data record from the bitmap array
    Bitmap getBitmap(String imageUriString) {

        if (imageUriString == null) { return null; }
        if (mBitmaps.containsKey(imageUriString)) {
            return mBitmaps.get(imageUriString);
        } else {
            return null;
        }
    }

    // Indicate if the specified image path exists in the bitmap store
    boolean contains(String imageUriString) {
        return mBitmaps.containsKey(imageUriString);
    }
}
