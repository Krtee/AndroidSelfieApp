package com.example.homeactivity;

import android.hardware.Camera;
import android.util.Log;

public class FaceDetectionListener implements Camera.FaceDetectionListener {
    FaceOverlayView overlayView;

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera){
        if (faces.length > 0){
            Log.d("FaceDetection", "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
            System.out.println(faces);
            overlayView.setFaces(faces);
        }
    }
}
