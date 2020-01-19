package com.example.homeactivity;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public class FaceOverlay extends RelativeLayout {
    private FirebaseVisionFace face;
    private ImageView image;
    private Rect facerect;
    private Filter filter;
    private ViewGroup.LayoutParams screen;
    private double scaleX,scaleY,scaleXY;

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

        Glide.with(this.getContext())
                .load(filter.getPic())
                .into(image);
    }

    void setFace(FirebaseVisionFace face) {
        this.face = face;
        facerect = face.getBoundingBox();
    }



    void rePosistion(){
        this.setX((float)(screen.width-(screen.width*scaleX)-(float)this.getWidth()/(float)2.5));
        this.setY((float)(screen.height*scaleY-(float) this.getHeight()/(float)1.3)) ;
        invalidate();
    }

    void setScale(double x, double y){
        scaleX= x;
        scaleY=y;
        scaleXY=(double)screen.width/screen.height;
    }

    double[] getScaledCentre(){
        double[] coords = new double[4];
        coords[0]= this.getX()/screen.width;
        coords[1]=this.getY()/screen.height;
        coords[2]=(this.getX()+this.getWidth())/screen.width;
        coords[3]=(this.getY()+this.getHeight())/screen.height;
        return coords;
    }

    void setFilter(Filter filter){
        this.filter=filter;
    }

    void setScreen(ViewGroup.LayoutParams screen) {
        this.screen = screen;
    }

    Filter getFilter(){
        return filter;
    }

    double getScaleXY() {
        return scaleXY;
    }

    ImageView getImage(){
        return image;
    }

}
