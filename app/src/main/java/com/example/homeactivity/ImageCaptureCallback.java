package com.example.homeactivity;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public interface ImageCaptureCallback {
    void onImageCaptured(File image, Bitmap bitmap, Uri fileUri);
}
