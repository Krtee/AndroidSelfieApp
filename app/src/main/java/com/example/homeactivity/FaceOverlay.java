package com.example.homeactivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FaceOverlay extends RelativeLayout {
    FirebaseVisionFace face;
    ImageView image;
    Rect facerect;
    Rect rect;
    String path;
    Filter filter;
    ViewGroup.LayoutParams screen;

    public FaceOverlay(Context context){
        super(context);
    }


    public FaceOverlay(Context context, AttributeSet attrs){
        super(context,attrs);
    }


    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    public void init(Context context){
        View v =LayoutInflater.from(context).inflate(R.layout.filter_view,this);
        image = findViewById(R. id.imagefilter);

        image.setImageResource(filter.getDrawable());
    }

    public void setFace(FirebaseVisionFace face) {
        this.face = face;
        facerect = face.getBoundingBox();
        rect = new Rect(facerect.left * filter.getScalingfact(),facerect.top * filter.getScalingfact(),facerect.right * filter.getScalingfact(),facerect.bottom * filter.getScalingfact());
    }



    public void rePosistion(){
        this.setX(screen.width-facerect.centerX()+this.getWidth()/2);
        this.setY(facerect.centerY()+this.getHeight()/2);
        invalidate();
    }

    public void setFilter(Filter filter){
        this.filter=filter;
    }

    public Filter getFilter(){
        return filter;
    }


    public Rect getRect() {
        return rect;
    }


    public String getPath() {
        return path;
    }

    public void setScreen(ViewGroup.LayoutParams screen) {
        this.screen = screen;
    }
}
