package com.example.homeactivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

    public static Bitmap createPic(Context context,Bitmap pic,int drawable, Rect rect, File file){
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
        return newPic;
    }

    public void createPic(Context context,FirebaseVisionFace face, Rect rect,Bitmap pic){
        FaceOverlay photofilter = new FaceOverlay(context);
        photofilter.setFace(face);


        photofilter.setLayoutParams(new FrameLayout.LayoutParams(rect.width(),rect.height()));
        photofilter.setScreen(new FrameLayout.LayoutParams(pic.getWidth(),pic.getHeight()));
        photofilter.rePosistion();
        System.out.println(photofilter.screen.width+ " "+photofilter.image.getWidth()+ " "+ pic.getWidth());
        Bitmap filter = Helper.loadBitmapFromView(photofilter);

        //Bitmap newPic = Helper.overlay(pic, filter,photofilter.getMatrix());

        SavedImage toSave = new SavedImage(context);
        toSave.init(pic,photofilter);
        Bitmap savebitmap = Helper.loadBitmapFromView(toSave);
    }

    public static Bitmap resize(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
    public static List<Filter> getAllFilters(Context context){
        List<Filter> filters = new ArrayList<>();
        try {
            Resources res = context.getResources();
            InputStreamReader in_s = new InputStreamReader(res.openRawResource(R.raw.pics));

            BufferedReader reader= new BufferedReader(in_s);
            String line;
            String[] infos;

            while((line=reader.readLine())!=null &&line.length()!=0){
                infos=line.split(",");
                int resID =context.getResources().getIdentifier(infos[0],"drawable",context.getPackageName());
                try{
                    filters.add(new Filter(resID,Integer.parseInt(infos[1])));
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return filters;
    }

}
