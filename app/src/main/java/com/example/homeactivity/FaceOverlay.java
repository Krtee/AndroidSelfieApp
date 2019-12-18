package com.example.homeactivity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.face.Face;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FaceOverlay extends View {
    FirebaseVisionFace face;
    ImageView image;
    Rect facerect;
    

    public FaceOverlay(Context context, AttributeSet attrs){
        super(context,attrs);
        File imgFile = new File("\\app\\src\\main\\res\\drawable\\peachemoji.png");
        if(imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            image.setImageBitmap(myBitmap);

        }


    }

    public void setFace(FirebaseVisionFace face) {
        this.face = face;
        facerect = face.getBoundingBox();
    }



    public void rePosistion(){
        this.setX(facerect.centerX());
        this.setY(facerect.centerY());
        invalidate();
    }

    public Rect getFacerect() {
        return facerect;
    }
}
