package com.example.homeactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Handler;

public class MainActivity extends Activity implements LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    private ImageCapture imageCapture;
    private Preview preview;
    int DSI_height,DSI_width;
    private Size imageDimension;
    ImageAnalysis imageAnalysis;
    CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);

        textureView = findViewById(R.id.imageView);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        Button createImg = findViewById(R.id.button_capture);

        ConstraintLayout btnparent= findViewById(R.id.button_parent);
        btnparent.bringChildToFront(createImg);

        //ViewCompat.setTranslationZ(createImg,1);

        createImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage(image -> runOnUiThread(() -> {
                    Intent myIntent = new Intent(MainActivity.this, PreviewActivity.class);
                    myIntent.setData(Uri.fromFile(image));
                    startActivity(myIntent);
                    })
                );
            }
        });
    }

    private void startCamera() {

        CameraX.unbindAll();

        imageAnalysis=createAnalyzer();
        preview =createPreview();
        imageCapture =createImageCapture();

        CameraX.bindToLifecycle((LifecycleOwner)this, imageAnalysis, preview, imageCapture);
    }

    public Preview createPreview(){
        int aspRatioW = textureView.getWidth(); //get width of screen
        int aspRatioH = textureView.getHeight();//get height
        setAspectRatioTextureView(720,1280);
        Rational asp = new Rational (aspRatioW, aspRatioH); //aspect ratio
        imageDimension = new Size(aspRatioW, aspRatioH);

        PreviewConfig.Builder previewConfigBuilder = new PreviewConfig.Builder();
        previewConfigBuilder
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetAspectRatio(asp)
                .setTargetResolution(imageDimension)
                .setLensFacing(CameraX.LensFacing.FRONT);

        PreviewConfig previewConfig = previewConfigBuilder.build();

        preview = new Preview(previewConfig);
        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        //updateTransform();
                    }
                });
        return preview;
    }


    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        System.out.println("W: "+ viewWidth+ " H: "+ viewHeight);
        textureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }

    private ImageCapture createImageCapture() {
        ImageCaptureConfig.Builder imageCaptureConfig = new ImageCaptureConfig.Builder();
        imageCaptureConfig.setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY);
        imageCaptureConfig.setLensFacing(CameraX.LensFacing.FRONT);

        // Build the image capture use case and attach button click listener
        imageCapture = new ImageCapture(imageCaptureConfig.build());

        return imageCapture;
    }



    void captureImage(final ImageCaptureCallback imageCaptureCallback) {
        if (imageCapture != null) {



            File path = MainActivity.this.getFilesDir();

            File file = new File(path, System.currentTimeMillis()+".png");

            imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    Toast.makeText(MainActivity.this,"Img captured",Toast.LENGTH_SHORT).show();
                    imageCaptureCallback.onImageCaptured(file);
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Toast.makeText(MainActivity.this,
                            message,
                            Toast.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(
                    MainActivity.this,
                    "You may need to grant the camera permission!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }


    public FaceDetector setFaceDetection(){
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory())
                        .build());

        cameraSource = new CameraSource.Builder(MainActivity.this, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();

        return detector;

    }




    private ImageAnalysis createAnalyzer() {
        ImageAnalysisConfig.Builder analyzerConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_NEXT_IMAGE);

        // To have a pretty quick analysis a resolution is enough.
        Size analyzeResolution = new Size(108, 192);
        analyzerConfig.setTargetResolution(analyzeResolution);
        analyzerConfig.setTargetAspectRatio(new Rational(analyzeResolution.getWidth(), analyzeResolution.getHeight()));
        analyzerConfig.setLensFacing(CameraX.LensFacing.FRONT);

        /*mFaceDetector analyzer = new mFaceDetector();
        analyzer.setFaceDetectionListener(new mFaceDetector().FaceDetectionListener() {
            @Override
            public void onFaceDetected(int faces) {
                if (faceDetectionListener != null) {
                    if (lastDetectedFaces != faces) {
                        lastDetectedFaces = faces;
                        faceDetectionListener.onFaceDetected(faces);
                    }
                }
            }

            @Override
            public void onNoFaceDetected() {
                if (faceDetectionListener != null) {
                    if (lastDetectedFaces > 0) {
                        lastDetectedFaces = 0;
                        faceDetectionListener.onNoFaceDetected();
                    }
                }
            }
        });*/
        imageAnalysis = new ImageAnalysis(analyzerConfig.build());
        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        // insert your code here.
                    }
                });
        return imageAnalysis;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }


}

interface ImageCaptureCallback {
    void onImageCaptured(File image);
}
