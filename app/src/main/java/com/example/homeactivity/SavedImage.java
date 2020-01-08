package com.example.homeactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SavedImage extends RelativeLayout {
    ImageView photo;
    FaceOverlay filter;
    Context context;

    public SavedImage(Context context){
        super(context);
        this.context = context;
    }


    public SavedImage(Context context, AttributeSet attrs){
        super(context,attrs);
        this.context = context;
    }

    public void init(Bitmap pic, FaceOverlay filter){
        View v = LayoutInflater.from(context).inflate(R.layout.savedimage_view,this);
        photo = findViewById(R. id.imageView4);
        this.filter = findViewById(R.id.facefilter);
        this.filter= filter;
        photo.setImageBitmap(pic);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = photo.getWidth();
        int desiredHeight = photo.getHeight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }
}
