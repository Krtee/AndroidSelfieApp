package com.example.homeactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageSaver extends Thread{
    File file;
    Context context;
    int drawable;
    ImageCaptureCallback imageCaptureCallback;
    View view;

    public ImageSaver(Context context,File file,int drawable,ImageCaptureCallback imageCaptureCallback, View view){
        this.context=context;
        this.file=file;
        this.drawable=drawable;
        this.imageCaptureCallback =imageCaptureCallback;
        this.view = view;
    }

    @Override
    public void run() {
        Bitmap pic;
        try {
            pic = Helper.rotateandflipBitmap(file, 90);
            //Bitmap pic = BitmapFactory.decodeFile(file.getAbsolutePath());
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(pic);

            FirebaseVisionFaceDetector detectImage = CameraManager.createDetector();

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
                                            Bitmap newPic = Helper.createPic(context, pic, drawable, rect, file);
                                            Bitmap scaled = Helper.resize(newPic,view.getWidth(),view.getHeight());
                                            imageCaptureCallback.onImageCaptured(scaled);


                                        }
                                    }
                                }
                            }
                    )
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    )
            ;


        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
