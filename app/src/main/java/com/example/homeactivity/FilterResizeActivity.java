package com.example.homeactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FilterResizeActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    private Preview preview;
    int DSI_height,DSI_width;
    ImageAnalysis imageAnalysis;
    FaceOverlay faceOverlay;
    boolean overlayShown = false;
    FirebaseVisionFaceDetector detector;
    Filter filter;
    Uri fileUri;
    String fileName;
    int scalingfact=2000;
    FrameLayout photoframe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filter_resize);

        ActionBar actionBar = FilterResizeActivity.this.getSupportActionBar();
        actionBar.setTitle("Configure the Size of your Filter!");

        Intent intent = getIntent();
        fileUri = Uri.parse(intent.getStringExtra("imagepath"));
        fileName = intent.getStringExtra("imagename");

        filter = new Filter(fileUri,scalingfact);

        textureView = findViewById(R.id.photoView);
        photoframe = findViewById(R.id.photoframe);
        faceOverlay= findViewById(R.id.cutfilter);
        faceOverlay.setVisibility(View.GONE);
        faceOverlay.setFilter(filter);
        faceOverlay.init(FilterResizeActivity.this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;

        cameraManager= new CameraManager(DSI_height,DSI_width);

        ViewCompat.setZ(faceOverlay,2);

        setButtons();

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    /*buttons for increasing,decreasing and saving the scalingfact*/
    private void setButtons(){
        Button small = findViewById(R.id.small);
        small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scalingfact-=50;
                filter.setScalingfact(scalingfact);
            }
        });

        Button big = findViewById(R.id.big);
        big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scalingfact+=50;
                filter.setScalingfact(scalingfact);
            }
        });

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File textfiles = FilterResizeActivity.this.getDir("textfiles", Context.MODE_PRIVATE);
                File txt = new File(textfiles,"filters.txt");
                try(FileWriter fw = new FileWriter(txt, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw))
                {
                    out.println(fileName+","+scalingfact);
                } catch (IOException e) {
                    Toast.makeText(FilterResizeActivity.this,"Error occured,try again",Toast.LENGTH_SHORT).show();
                }

                Intent intent1 = new Intent(FilterResizeActivity.this,SelectFilterActivity.class);
                startActivity(intent1);
                finish();
            }
        });
    }

     /*initialize Camera*/
    private void startCamera() {

        CameraX.unbindAll();

        detector = CameraManager.createDetector();
        textureView.setLayoutParams(new FrameLayout.LayoutParams(DSI_width,DSI_height));
        preview =cameraManager.createPreview();
        setPreviewlistener();
        faceOverlay.setScreen(textureView.getLayoutParams());
        imageAnalysis=setAnalyzerTask();

        CameraX.bindToLifecycle(FilterResizeActivity.this, imageAnalysis, preview);
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

    /*analize downscaled preview picture, Facdetector recognize face and draws Retangle around it, also scaling filter over it*/
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
                                            System.out.println(faceOverlay.getX()+ "! "+ faceOverlay.getWidth());
                                            if(!overlayShown){
                                                faceOverlay.setVisibility(View.VISIBLE);
                                                overlayShown= true;
                                            }
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
        finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        CameraX.unbindAll();
        finish();
    }
}
