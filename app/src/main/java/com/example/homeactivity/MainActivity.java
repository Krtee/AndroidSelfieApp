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
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Layout;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    FrameLayout overlayframe;
    FrameLayout picframe;
    FaceOverlay faceOverlay;
    boolean overlayShown = false;
    FirebaseVisionFaceDetector detector;
    ImageButton gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);

        textureView = findViewById(R.id.imageView);
        layout = findViewById(R.id.linearLayout);
        overlayframe = findViewById(R.id.overlayposi);
        picframe = findViewById(R.id.imageframe);
        gallery = findViewById(R.id.gallery);

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

        ImageButton createImg = findViewById(R.id.button_capture);

        ConstraintLayout btnparent= findViewById(R.id.bottomBar);
        btnparent.bringChildToFront(createImg);
        createImg.invalidate();

        ViewCompat.setZ(faceOverlay,2);
        ViewCompat.setZ(btnparent,1);

        createImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage(image -> new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Worked.",Toast.LENGTH_LONG).show();
                        gallery.setImageURI(Uri.fromFile());
                    }
                }).start()
                );
            }
        });
    }

    private void startCamera() {

        CameraX.unbindAll();

        detector = createDetector();
        imageAnalysis=createAnalyzer();
        preview =createPreview();
        imageCapture =createImageCapture();

        CameraX.bindToLifecycle(this, imageAnalysis, preview, imageCapture);
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
        //picframe.setLayoutParams(textureView.getLayoutParams());

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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap bits = Helper.rotateandflipBitmap(file, 90);
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "Couldnt rotate Picture", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(MainActivity.this, String.valueOf(overlayframe.getWidth()), Toast.LENGTH_SHORT).show();


                            Bitmap pic = BitmapFactory.decodeFile(file.getAbsolutePath());
                            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(pic);

                            FirebaseVisionFaceDetector detectImage = createDetector();

                            detectImage.detectInImage(image)
                                    .addOnSuccessListener(
                                            new OnSuccessListener<List<FirebaseVisionFace>>() {
                                                @Override
                                                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                                                    if (firebaseVisionFaces != null && firebaseVisionFaces.size() != 0) {
                                                        for (FirebaseVisionFace face : firebaseVisionFaces) {

                                                            Rect rect = face.getBoundingBox();
                                                            System.out.println("Rect:" + rect.width() + " " + rect.height());
                                                            int scalingfact = 1;
                                                            rect.set(rect.left * scalingfact, rect.top * scalingfact, rect.right * scalingfact, rect.bottom * scalingfact);

                                                    /*
                                                    FaceOverlay photofilter = new FaceOverlay(MainActivity.this);
                                                    photofilter.setFace(face);


                                                    photofilter.setLayoutParams(new FrameLayout.LayoutParams(rect.width(),rect.height()));
                                                    photofilter.setScreen(new FrameLayout.LayoutParams(pic.getWidth(),pic.getHeight()));
                                                    photofilter.rePosistion();
                                                    System.out.println(photofilter.screen.width+ " "+photofilter.image.getWidth()+ " "+ pic.getWidth());
                                                    Bitmap filter = Helper.loadBitmapFromView(photofilter);

                                                    Bitmap newPic = Helper.overlay(pic, filter,photofilter.getMatrix());

                                                    SavedImage toSave = new SavedImage(MainActivity.this);
                                                    toSave.init(pic,photofilter);
                                                    Bitmap savebitmap = Helper.loadBitmapFromView(toSave);
                                                    */

                                                            Helper.createPic(MainActivity.this, pic, faceOverlay.getFilter(), rect, file);
                                                            imageCaptureCallback.onImageCaptured(file);


                                                        }
                                                    }
                                                }
                                            }
                                    )
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println(e.getCause());
                                                }
                                            });


                        }
                    }).start();
                    Toast.makeText(MainActivity.this, "Img captured", Toast.LENGTH_SHORT).show();
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

    private FirebaseVisionFaceDetector createDetector(){
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build();



        FirebaseVisionFaceDetector d = FirebaseVision.getInstance().getVisionFaceDetector(options);
        return d;

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
                                            //System.out.println(faceOverlay.image.getWidth());
                                            if(!overlayShown){
                                                faceOverlay.setVisibility(View.VISIBLE);
                                                overlayShown= true;
                                            }
                                            //System.out.println(overlayframe.getLayoutParams().width);

                                            int[] loc =new int[2];
                                            faceOverlay.image.getLocationOnScreen(loc);

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

    public interface ImageCaptureCallback {
        void onImageCaptured(File image);
    }

    /*private class SaveImage extends AsyncTask<File,Void,File>{
        protected File doInBackground(File... files) {
            Toast.makeText(MainActivity.this,"Img captured",Toast.LENGTH_SHORT).show();
            try{
                Bitmap bits= Helper.rotateandflipBitmap(files[0],90);
            }
            catch (IOException e){
                Toast.makeText(MainActivity.this,"Couldnt rotate Picture",Toast.LENGTH_SHORT).show();
            }

            FirebaseVisionFaceDetector imagedetect = createDetector();

            Bitmap pic= BitmapFactory.decodeFile(files[0].getAbsolutePath());
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(pic);

            imagedetect.detectInImage(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                                    if(firebaseVisionFaces!= null&&firebaseVisionFaces.size()!=0) {
                                        for (FirebaseVisionFace face : firebaseVisionFaces) {

                                            Rect rect = face.getBoundingBox();
                                            System.out.println("Rect:"+ rect.width()+" " + rect.height());
                                            int scalingfact =1;
                                            rect.set(rect.left * scalingfact, rect.top *scalingfact, rect.right *scalingfact, rect.bottom * scalingfact);
                                                    /*FaceOverlay photofilter = new FaceOverlay(MainActivity.this);
                                                    photofilter.setFace(face);


                                                    photofilter.setLayoutParams(new FrameLayout.LayoutParams(rect.width(),rect.height()));
                                                    photofilter.setScreen(new FrameLayout.LayoutParams(pic.getWidth(),pic.getHeight()));
                                                    photofilter.rePosistion();
                                                    System.out.println(photofilter.screen.width+ " "+photofilter.image.getWidth()+ " "+ pic.getWidth());
                                                    Bitmap filter = Helper.loadBitmapFromView(photofilter);

                                                    Bitmap newPic = Helper.overlay(pic, filter,photofilter.getMatrix());

                                                    SavedImage toSave = new SavedImage(MainActivity.this);
                                                    toSave.init(pic,photofilter);
                                                    Bitmap savebitmap = Helper.loadBitmapFromView(toSave);

                                            Helper.createPic(MainActivity.this,pic,faceOverlay.getFilter(),rect,files[0]);

                                        }
                                    }
                                }
                            }
                    )
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getCause());
                                }
                            });

        }


        protected void onPostExecute(File result) {


        }
    }

     */
}
