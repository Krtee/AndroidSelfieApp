package com.example.homeactivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Helper {

    public static Bitmap rotateandflipBitmap(File file, float angle) throws IOException {
        Bitmap source;
        source = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        matrix.postRotate(angle);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return rotated;

    }
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, Rect rect) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, null,rect, null);
        return bmOverlay;
    }
    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public static File BitmaptoFile(Bitmap map, File file) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return file;
    }

    public static void createPic(Context context,Bitmap pic,int drawable, Rect rect, File file){
        Resources resources = context.getResources();
        Bitmap picmap = BitmapFactory.decodeResource(resources,drawable);
        Bitmap.createScaledBitmap(picmap,rect.width(),rect.height(),false);
        System.out.println(pic.getHeight()+" "+ pic.getWidth()+ " " +picmap.getHeight()+ " " + picmap.getWidth());
        Bitmap newPic = Helper.overlay(pic, picmap ,rect);

        try{
            File newfile = Helper.BitmaptoFile(newPic,file);
        }
        catch (IOException e){
            Toast.makeText(context,"Couldn#t put filter on Pic.",Toast.LENGTH_LONG).show();
        }
    }

}
