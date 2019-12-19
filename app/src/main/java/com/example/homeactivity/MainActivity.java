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
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;

import static androidx.constraintlayout.widget.Constraints.TAG;

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
    FrameLayout layout;
    FaceOverlay faceOverlay;
    boolean overlayShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);

        textureView = findViewById(R.id.imageView);
        layout = findViewById(R.id.linearLayout);
        faceOverlay= findViewById(R.id.filter);
        faceOverlay.setVisibility(View.GONE);


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
        createImg.invalidate();

        ViewCompat.setZ(faceOverlay,1);
        ViewCompat.setZ(btnparent,2);

        createImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    System.out.println(layout.getChildAt(i).getTranslationZ());
                }
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
        int aspRatioW = textureView.getWidth();
        int aspRatioH = textureView.getHeight();
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
        faceOverlay.setScreen(textureView.getLayoutParams());

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

            ContextWrapper cw = new ContextWrapper(getApplicationContext());

            File path = cw.getDir("imageDir", Context.MODE_PRIVATE);

            File file = new File(path, System.currentTimeMillis()+".png");


            imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    Toast.makeText(MainActivity.this,"Img captured",Toast.LENGTH_SHORT).show();
                    try{
                        Bitmap bits= Helper.rotateandflipBitmap(file,90);
                    }
                    catch (IOException e){
                        Toast.makeText(MainActivity.this,"Couldnt rotate Picture",Toast.LENGTH_SHORT).show();
                    }
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


    private ImageAnalysis createAnalyzer() {
        HandlerThread analyzerThread = new HandlerThread("FaceDetectionAnalyzer");
        analyzerThread.start();

        ImageAnalysisConfig.Builder analyzerConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()));

        // To have a pretty quick analysis a resolution is enough.
        Size analyzeResolution = new Size(86, 150);
        analyzerConfig.setTargetResolution(analyzeResolution);
        analyzerConfig.setTargetAspectRatio(new Rational(analyzeResolution.getWidth(), analyzeResolution.getHeight()));
        analyzerConfig.setLensFacing(CameraX.LensFacing.FRONT);

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build();



        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);


        imageAnalysis = new ImageAnalysis(analyzerConfig.build());
        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        if (image == null || image.getImage() == null) {
                            return;
                        }
                        Image mediaImage = image.getImage();
                        FirebaseVisionImage visionImage =
                                FirebaseVisionImage.fromMediaImage(mediaImage, FirebaseVisionImageMetadata.ROTATION_270);


                        detector.detectInImage(visionImage)
                                .addOnSuccessListener(faces -> {
                                    if(faces!= null&&faces.size()!=0) {
                                        for (FirebaseVisionFace face : faces) {

                                            faceOverlay.setFace(face);
                                            Rect rect = face.getBoundingBox();
                                            faceOverlay.setLayoutParams(new FrameLayout.LayoutParams(rect.width(),rect.height()));
                                            faceOverlay.rePosistion();
                                            faceOverlay.image.invalidate();
                                            if(!overlayShown){
                                                faceOverlay.setVisibility(View.VISIBLE);
                                                overlayShown= true;
                                            }

                                            int[] loc =new int[2];
                                            faceOverlay.image.getLocationOnScreen(loc);

                                            System.out.println(loc[0]);

                                        }
                                    }
                                    else{
                                        faceOverlay.setVisibility(View.GONE);
                                        overlayShown=false;
                                    }
                                })
                                .addOnFailureListener(stuff ->{System.out.println("fuck.");});
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
    public void onPause(){
        super.onPause();
        CameraX.unbindAll();
    }

    @Override
    public void onStop(){
        super.onStop();
        CameraX.unbindAll();
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

    interface ImageCaptureCallback {
        void onImageCaptured(File image);
    }
}
