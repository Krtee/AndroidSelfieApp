package com.example.homeactivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CropActivity extends AppCompatActivity implements View.OnTouchListener{
    ImageView croppedImage;
    Path clipPath;
    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    float downx = 0;
    float downy = 0;
    float tdownx = 0;
    float tdowny = 0;
    float upx = 0;
    float upy = 0;
    long lastTouchDown = 0;
    int CLICK_ACTION_THRESHHOLD = 100;
    Display display;
    Point size;
    int screen_width,screen_height;
    Button btn_ok;
    ArrayList<CropModel> cropModelArrayList;
    float smallx,smally,largex,largey;
    Paint cpaint;
    Bitmap temporary_bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_activity);

        ActionBar actionBar = CropActivity.this.getSupportActionBar();
        actionBar.setTitle("Crop your Filter!");


        croppedImage = (ImageView) findViewById(R.id.im_crop_image_view);
        cropModelArrayList = new ArrayList<>();
        btn_ok = (Button) findViewById(R.id.cropbutton);
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        initcanvas();


        int cx = (screen_width - bmp.getWidth()) >> 1;
        int cy = (screen_height - bmp.getHeight()) >> 1;
        canvas.drawBitmap(bmp, cx, cy, null);
        croppedImage.setImageBitmap(alteredBitmap);
        croppedImage.setOnTouchListener(this);
    }

    /*initialize cropping image*/
    @SuppressWarnings("deprecation")
    void initcanvas() {
        Intent intent= getIntent();

        Uri uri =Uri.parse(intent.getStringExtra("pic"));

        try {
            if(Build.VERSION.SDK_INT<28){
                bmp= MediaStore.Images.Media.getBitmap(CropActivity.this.getContentResolver(),uri);
            }
            else {
                ImageDecoder.Source source = ImageDecoder.createSource(CropActivity.this.getContentResolver(), uri);
                Bitmap decodeBitmap = ImageDecoder.decodeBitmap(source);
                bmp=decodeBitmap.copy(Bitmap.Config.ARGB_8888,false);
            }
        }
        catch (IOException e){
            Toast.makeText(CropActivity.this,"No image could be loaded.",Toast.LENGTH_SHORT).show();
            finish();
        }


        alteredBitmap = Bitmap.createBitmap(screen_width, screen_height,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(alteredBitmap);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

    }

    /*Method for saving touchpoints ord deleting them*/
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:


                downx = event.getX();
                downy = event.getY();
                clipPath = new Path();
                clipPath.moveTo(downx, downy);
                tdownx = downx;
                tdowny = downy;
                smallx = downx;
                smally = downy;
                largex = downx;
                largey = downy;
                lastTouchDown = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                upx = event.getX();
                upy = event.getY();
                cropModelArrayList.add(new CropModel(upx, upy));
                clipPath = new Path();
                clipPath.moveTo(tdownx,tdowny);
                for(int i = 0; i<cropModelArrayList.size();i++){
                    clipPath.lineTo(cropModelArrayList.get(i).getY(),cropModelArrayList.get(i).getX());
                }
                canvas.drawPath(clipPath, paint);
                croppedImage.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {

                    cropModelArrayList.clear();
                    initcanvas();

                    int cx = (screen_width - bmp.getWidth()) >> 1;
                    int cy = (screen_height - bmp.getHeight()) >> 1;
                    canvas.drawBitmap(bmp, cx, cy, null);
                    croppedImage.setImageBitmap(alteredBitmap);

                } else {
                    if (upx != upy) {
                        upx = event.getX();
                        upy = event.getY();


                        canvas.drawLine(downx, downy, upx, upy, paint);
                        clipPath.lineTo(upx, upy);
                        croppedImage.invalidate();

                        crop();

                        btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                save();
                            }
                        });
                    }

                }
                break;
            default:
                break;
        }
        return true;
    }

    /*marking cropped image*/
    public void crop() {

        clipPath.close();
        clipPath.setFillType(Path.FillType.INVERSE_WINDING);

        for(int i = 0; i<cropModelArrayList.size();i++){
            if(cropModelArrayList.get(i).getY()<smallx){

                smallx=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()<smally){

                smally=cropModelArrayList.get(i).getX();
            }
            if(cropModelArrayList.get(i).getY()>largex){

                largex=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()>largey){

                largey=cropModelArrayList.get(i).getX();
            }
        }

        temporary_bitmap = alteredBitmap;
        cpaint = new Paint();
        cpaint.setAntiAlias(true);
        cpaint.setColor(getResources().getColor(R.color.colorAccent));
        cpaint.setAlpha(100);
        canvas.drawPath(clipPath, cpaint);

        canvas.drawBitmap(temporary_bitmap, 0, 0, cpaint);

    }

    /*save cropped to internalstorage*/
    private void save() {

        if(clipPath != null) {
            final int color = 0xff424242;
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPath(clipPath, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(alteredBitmap, 0, 0, paint);

            float w = largex - smallx;
            float h = largey - smally;
            alteredBitmap = Bitmap.createBitmap(alteredBitmap, (int) smallx, (int) smally, (int) w, (int) h);

        }else{
            alteredBitmap = bmp;
        }

        Thread mThread = new Thread() {
            @Override
            public void run() {

                Bitmap bitmap = alteredBitmap;
                File newdir= CropActivity.this.getDir("filters", Context.MODE_PRIVATE);
                Intent intent = new Intent(CropActivity.this, FilterResizeActivity.class);
                File file = new File(newdir,  +System.currentTimeMillis()+ ".png");
                try {
                    FileOutputStream outStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                }
                catch (IOException e){
                    Toast.makeText(CropActivity.this,"Couldnt save filter",Toast.LENGTH_SHORT).show();
                }
                intent.putExtra("imagename",file.getName());
                intent.putExtra("imagepath",Uri.fromFile(file).toString());
                startActivity(intent);
                finish();
            }
        };
        mThread.start();

    }
}
