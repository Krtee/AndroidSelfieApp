package com.example.homeactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    private ImageCapture imageCapture;
    private Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.imageView);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        Button createImg = findViewById(R.id.button_capture);

        createImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    private void startCamera() {

        CameraX.unbindAll();


        preview =createPreview();
        imageCapture =createImageCapture();
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imageCapture);
    }

    public Preview createPreview(){
        PreviewConfig.Builder previewConfigBuilder = new PreviewConfig.Builder();
        previewConfigBuilder
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
                        updateTransform();
                    }
                });
        return preview;
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



    interface ImageCaptureCallback {
        void onImageCaptured(File image);
    }



}
