package com.example.homeactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
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
import android.graphics.Rect;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements LifecycleOwner {
    private CameraManager cameraManager;
    private LifecycleRegistry lifecycleRegistry;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    private ImageCapture imageCapture;
    private Preview preview;
    int DSI_height,DSI_width;
    ImageAnalysis imageAnalysis;
    FaceOverlay faceOverlay;
    boolean overlayShown = false;
    FirebaseVisionFaceDetector detector;
    ImageButton gallery;
    ProgressBar progressBar;
    Filter filter;
    File file;
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        filter =intent.getParcelableExtra("filtermask");

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);

        textureView = findViewById(R.id.imageView);
        gallery = findViewById(R.id.gallery);
        progressBar= findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        ConstraintLayout progressparent = findViewById(R.id.progressBarpnt);

        faceOverlay= findViewById(R.id.filter);
        faceOverlay.setVisibility(View.GONE);
        faceOverlay.setFilter(filter);
        faceOverlay.init(MainActivity.this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;

        cameraManager= new CameraManager(DSI_height,DSI_width);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        ImageButton createImg = findViewById(R.id.button_capture);

        ConstraintLayout btnparent= findViewById(R.id.bottomBar);
        btnparent.bringChildToFront(createImg);
        createImg.invalidate();

        ViewCompat.setZ(faceOverlay,2);
        ViewCompat.setZ(btnparent,1);
        ViewCompat.setZ(progressparent,3);

        createImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage((image,bitmap,fileUri) -> runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gallery.setImageBitmap(bitmap);
                                setFileUri(fileUri);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this,"Worked.",Toast.LENGTH_LONG).show();
                            }
                        })
                );
            }
        });

        ImageButton back = findViewById(R.id.BackButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                finish();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileUri!=null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this,"No picture taken yet.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startCamera() {

        CameraX.unbindAll();

        detector = CameraManager.createDetector();
        textureView.setLayoutParams(new FrameLayout.LayoutParams(DSI_width,DSI_height));
        preview =cameraManager.createPreview();
        setPreviewlistener();
        faceOverlay.setScreen(textureView.getLayoutParams());
        imageAnalysis=setAnalyzerTask();
        imageCapture =CameraManager.createImageCapture();

        CameraX.bindToLifecycle(this, imageAnalysis, preview, imageCapture);
    }

    private void setPreviewlistener(){
        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                    }
                });
    }

    void captureImage(final ImageCaptureCallback imageCaptureCallback) {
        if (imageCapture != null) {

            File[] root = ContextCompat.getExternalFilesDirs(this,"images");
            File myDir = root[0];
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            file = new File(myDir, System.currentTimeMillis()+".jpg");

            FaceOverlay picOverlay = faceOverlay;


            imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    faceOverlay.setVisibility(View.GONE);
                    Thread savingPic = new ImageSaver(MainActivity.this,file,picOverlay,imageCaptureCallback,gallery);
                    savingPic.start();
                    System.out.println("pic taken"+picOverlay.getWidth());
                    Toast.makeText(MainActivity.this, "Img captured", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Toast.makeText(MainActivity.this, "couldn take picture",
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

    private ImageAnalysis setAnalyzerTask() {
        imageAnalysis =CameraManager.createAnalyser();
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
                                            double x = (double) rect.centerX()/90;
                                            double y = (double) rect.centerY()/120;
                                            faceOverlay.setScale(x,y);
                                            int rectWidth= (int)Math.round((double) rect.width()/90*faceOverlay.getFilter().getScalingfact());
                                            int rectHeight=(int)Math.round((double) rect.height()/120*faceOverlay.getFilter().getScalingfact());
                                            faceOverlay.rePosistion();
                                            faceOverlay.setLayoutParams(new FrameLayout.LayoutParams(rectWidth,rectHeight));
                                            faceOverlay.getImage().invalidate();
                                            //System.out.println(faceOverlay.getX()+ "! "+ faceOverlay.getWidth());
                                            if(!overlayShown){
                                                faceOverlay.setVisibility(View.VISIBLE);
                                                overlayShown= true;
                                            }
                                            //System.out.println(overlayframe.getLayoutParams().width);
                                            int[] loc =new int[2];
                                            faceOverlay.getImage().getLocationOnScreen(loc);

                                        }
                                    }
                                    else{
                                        faceOverlay.setVisibility(View.GONE);
                                        overlayShown=false;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getCause());
                                    }
                                });
                    }
                });




        return imageAnalysis;
    }
    private void setFileUri(Uri uri){
        this.fileUri=uri;
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
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

}
