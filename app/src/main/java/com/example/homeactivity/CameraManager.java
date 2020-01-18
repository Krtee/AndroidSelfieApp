package com.example.homeactivity;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

public class CameraManager {
    private int DSI_height,DSI_width;

    CameraManager(int DSI_height,int DSI_width){
        this.DSI_height=DSI_height;
        this.DSI_width= DSI_width;
    }

    Preview createPreview() {
        Rational rational=new Rational(DSI_width,DSI_height);

        PreviewConfig.Builder previewConfigBuilder = new PreviewConfig.Builder();
        previewConfigBuilder
                .setTargetAspectRatio(rational)
                .setTargetRotation(Surface.ROTATION_0)
                .setLensFacing(CameraX.LensFacing.FRONT);

        PreviewConfig previewConfig = previewConfigBuilder.build();
        //picframe.setLayoutParams(textureView.getLayoutParams());

        Preview preview = new Preview(previewConfig);

        return preview;
    }
/*
    FrameLayout.LayoutParams setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);
            return new FrameLayout.LayoutParams(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = DSI_height;  //((DSI_width * ResolutionHeight)/ResolutionWidth);
            return new FrameLayout.LayoutParams(newWidth,newHeight);
        }

    }

 */

    static ImageCapture createImageCapture() {
        ImageCaptureConfig.Builder imageCaptureConfig = new ImageCaptureConfig.Builder();
        imageCaptureConfig.setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY);
        imageCaptureConfig.setLensFacing(CameraX.LensFacing.FRONT);

        ImageCapture imageCapture = new ImageCapture(imageCaptureConfig.build());

        return imageCapture;
    }


    static FirebaseVisionFaceDetector createDetector(){
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build();

        FirebaseVisionFaceDetector d = FirebaseVision.getInstance().getVisionFaceDetector(options);
        return d;

    }

    static ImageAnalysis createAnalyser(){
        HandlerThread analyzerThread = new HandlerThread("FaceDetectionAnalyzer");
        analyzerThread.start();

        ImageAnalysisConfig.Builder analyzerConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()));

        // To have a pretty quick analysis a resolution is enough.
        Size analyzeResolution = new Size(90, 120);
        analyzerConfig.setTargetResolution(analyzeResolution);
        analyzerConfig.setTargetAspectRatio(new Rational(analyzeResolution.getWidth(), analyzeResolution.getHeight()));
        analyzerConfig.setLensFacing(CameraX.LensFacing.FRONT);

        ImageAnalysis imageAnalysis = new ImageAnalysis(analyzerConfig.build());
        return imageAnalysis;
    }


}
