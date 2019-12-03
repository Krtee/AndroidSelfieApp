package com.example.homeactivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.view.View;


public class FaceOverlayView extends View {
    private Paint mPaint;
    private Paint mTextPaint;
    private Camera.Face[] mFaces;

    public FaceOverlayView(Context context){
        super(context);
        initialize();
    }

    private void initialize(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(128);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(Camera.Face[] faces){
        mFaces = faces;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(mFaces !=null && mFaces.length>0){
            Matrix matrix = new Matrix();
            canvas.save();
            RectF rectF = new RectF();
            for (Camera.Face face: mFaces){
                rectF.set(face.rect);
                matrix.mapRect(rectF);
                canvas.drawRect(rectF, mPaint);
                canvas.drawText("Score " + face.score, rectF.right, rectF.top, mTextPaint);
            }
            canvas.restore();

        }
    }
}
